# Deployment on a VPS (Debian 13)

The shop runs on one VPS at https://flexstore.tech: PostgreSQL, the back end and the web server (Caddy) are Docker
containers, started by Docker Compose. Every merge on `main` whose CI is green deploys it.

```
Internet ── 80/443 ──> caddy ──> /api, /mcp, /actuator/health ──> app (back end, port 8080) ──> db (PostgreSQL)
                          └────> everything else: the pre-rendered front end (/opt/flexstore/front)
```

The front end and the API share one address, so the browser needs no CORS. Only the ports 22, 80 and 443 are open:
the database and the back end publish no port.

## What the deployment does

`.github/workflows/deploy.yml` runs after the `CI` workflow succeeds on `main` (or by hand, from the Actions tab):

1. builds the back end image with the root `Dockerfile` and pushes it to `ghcr.io/eolivier/flexstore-back`
   (tags: the commit and `latest`);
2. copies `deploy/docker-compose.prod.yml` and `deploy/Caddyfile` to `/opt/flexstore` on the VPS, writes its `.env`
   from the secrets and the variables of GitHub, pulls the image and restarts what changed (Flyway runs the new migrations when the back end starts);
3. waits for `https://flexstore.tech/actuator/health`;
4. builds the front end (`VITE_API_URL` and `VITE_SITE_URL` are the address of the shop) and copies `front/dist`
   to `/opt/flexstore/front`;
5. checks that the shop and the API answer.

Only one deployment runs at a time.

## Set up the VPS (once)

### 1. Packages and Docker (as root)

```bash
apt update && apt upgrade -y
apt install -y ca-certificates curl git ufw sudo rsync
curl -fsSL https://get.docker.com | sh
```

`rsync` is needed by the deployment to copy the front end.

### 2. The `deploy` user

The deployment signs in as `deploy`, which is in the `docker` group and owns `/opt/flexstore`.

```bash
adduser --disabled-password --gecos "" deploy
usermod -aG sudo,docker deploy
passwd deploy        # only used by sudo, never by SSH nor GitHub
install -d -m 700 -o deploy -g deploy /home/deploy/.ssh
install -d -o deploy -g deploy /opt/flexstore /opt/flexstore/front
```

A member of the `docker` group is as powerful as root on the machine: the key of the deployment is as sensitive as a
root key.

### 3. The SSH keys

A key tells **who** may sign in: the public keys go in `/home/deploy/.ssh/authorized_keys`, the private keys stay
with whoever signs in. Two keys are needed, made on your computer:

```bash
ssh-keygen -t ed25519 -C "github-actions-flexstore" -f ~/.ssh/flexstore_deploy -N ""
```

- your own key (`~/.ssh/id_ed25519.pub`), to sign in yourself;
- the key of the deployment (`~/.ssh/flexstore_deploy.pub`), without passphrase, for GitHub Actions only.

As root on the VPS, add each public key (one line) and set the rights:

```bash
echo 'ssh-ed25519 AAAA... you@laptop' >> /home/deploy/.ssh/authorized_keys
echo 'ssh-ed25519 AAAA... github-actions-flexstore' >> /home/deploy/.ssh/authorized_keys
chown deploy:deploy /home/deploy/.ssh/authorized_keys && chmod 600 /home/deploy/.ssh/authorized_keys
```

Check both keys from your computer before going further:

```bash
ssh deploy@VPS_IP 'echo personal key ok'
ssh -i ~/.ssh/flexstore_deploy -o IdentitiesOnly=yes deploy@VPS_IP 'docker ps'
```

### 4. Firewall and SSH hardening

```bash
ufw default deny incoming
ufw default allow outgoing
ufw limit 22/tcp
ufw allow 80/tcp
ufw allow 443/tcp
ufw enable
```

Docker bypasses `ufw` for the ports it publishes: that is why the stack publishes only 80 and 443.

Once `ssh deploy@VPS_IP` works with your key, refuse root and passwords. The first value read wins, so the file
is read before the others (check with `grep -ri passwordauthentication /etc/ssh/sshd_config.d/`):

```bash
cat > /etc/ssh/sshd_config.d/00-hardening.conf <<'EOF'
PermitRootLogin no
PasswordAuthentication no
EOF
sshd -t && systemctl reload ssh
sshd -T | grep -E 'permitrootlogin|passwordauthentication'    # no and no
```

Keep your root session open and test a new connection of `deploy` before closing it. The web console of the host
(hPanel, "Browser terminal") stays available if you lock yourself out.

### 5. DNS

An `A` record of `flexstore.tech` to the IP of the VPS. Caddy gets its certificate from Let's Encrypt when it
starts, so the name must already lead to the VPS (`dig +short flexstore.tech`).

There is nothing else to write on the server: its `.env` is generated at each deployment from the secrets and the
variables of GitHub (next section).

## Set up GitHub (once)

**Settings > Secrets and variables > Actions**. The workflow writes the `.env` of the server from them (`/opt/flexstore/.env`,
readable by `deploy` only), so the configuration lives in GitHub and nowhere else.

*Secrets* (tab Secrets > New repository secret):

| Secret | Value |
|---|---|
| `VPS_HOST` | IP address of the VPS |
| `VPS_USER` | `deploy` |
| `VPS_SSH_KEY` | the private key `~/.ssh/flexstore_deploy`, with its `BEGIN` and `END` lines |
| `VPS_KNOWN_HOSTS` | the output of `ssh-keyscan -t ed25519 VPS_IP` |
| `POSTGRES_PASSWORD` | letters and digits only: `openssl rand -hex 32` |
| `FLEXSTORE_JWT_SECRET` | 32 characters at least: `openssl rand -hex 32` |
| `FLEXSTORE_MAIL_USERNAME`, `FLEXSTORE_MAIL_PASSWORD` | credentials of the SMTP provider (optional) |
| `FLEXSTORE_STRIPE_SECRET_KEY` | `sk_live_...` (optional: without it the payments are simulated) |
| `FLEXSTORE_STRIPE_WEBHOOK_SECRET` | `whsec_...` (optional) |

*Variables* (tab Variables, not secret, all optional):

| Variable | Value |
|---|---|
| `FLEXSTORE_MAIL_HOST` | e.g. `smtp-relay.brevo.com` (without it the emails are written to the logs) |
| `FLEXSTORE_MAIL_PORT` | `587` by default |
| `FLEXSTORE_MAIL_FROM` | e.g. `Flexstore <no-reply@flexstore.tech>` |
| `FLEXSTORE_GOOGLE_CLIENT_ID` | the OAuth client id of Google (see README, *Sign in with Google*): given to the back end and, as `VITE_GOOGLE_CLIENT_ID`, to the build of the front end. Without it the button is not shown |

`POSTGRES_PASSWORD` and `FLEXSTORE_JWT_SECRET` are required: the deployment stops with an error when one is missing.
The values hold no quote and no line break. The name and the user of the database (`flexstore`, `flex`) and
`FLEXSTORE_FRONT_URL` (the address of the shop) are written by the workflow. To change a value, change it in GitHub and
run the workflow again.

**Change `POSTGRES_PASSWORD` only before the first deployment.** PostgreSQL stores it when it creates the database;
a new value in GitHub would then be refused by the database. To change it afterwards, change it in the database first
(`docker exec -it flexstore-db psql -U flex -d flexstore -c "ALTER USER flex PASSWORD '...'"`), then in GitHub.

GitHub never gives a secret back; replace it to change it. `VPS_KNOWN_HOSTS` pins the identity of the server: compare
`ssh-keyscan -t ed25519 VPS_IP | ssh-keygen -lf -` with `ssh-keygen -lf /etc/ssh/ssh_host_ed25519_key.pub` run on the
VPS.

The image holds no secret (they are in the `.env` of the server), so the package can be public: after the first run
of the workflow, **Packages > flexstore-back > Package settings > Change visibility > Public**. Otherwise the VPS must
sign in to `ghcr.io` with a token that can read packages (`docker login ghcr.io`).

## First deployment

Run the workflow by hand (**Actions > Deploy to the VPS > Run workflow**), then open https://flexstore.tech.

1. Sign in as `admin@flexstore.com` / `admin` and **change this password at once**: the migrations create the account
   with this well known password.
2. Stripe: add the webhook endpoint `https://flexstore.tech/api/payments/stripe/webhook` for the event
   `checkout.session.completed`, put its secret in the GitHub secret `FLEXSTORE_STRIPE_WEBHOOK_SECRET` and run the
   workflow again.
3. Back up the database, for instance a daily cron of `deploy`:

   ```bash
   docker exec flexstore-db pg_dump -U flex flexstore | gzip > /opt/flexstore/backup-$(date +%F).sql.gz
   ```

   Copy the backups out of the VPS: a backup on the same disk does not survive the loss of the disk.

## Everyday operations

```bash
cd /opt/flexstore
docker compose -f docker-compose.prod.yml ps
docker compose -f docker-compose.prod.yml logs -f app
docker compose -f docker-compose.prod.yml restart app
```

The `.env` is rewritten by every deployment: change the secrets and the variables in GitHub, not in this file.

**Roll back**: every deployed commit stays available as an image, so start the image of an earlier commit on the VPS:

```bash
IMAGE_TAG=<commit sha> docker compose -f docker-compose.prod.yml up -d app
```

The migrations already applied are not undone: a rollback across a migration needs a restored backup.

**Revoke the deployment**: remove its line (`github-actions-flexstore`) from `/home/deploy/.ssh/authorized_keys`;
your own access is not affected.

## Notes

- The Cloudflare Pages front end (`vue-pages.yml`) is no longer deployed: the workflow only starts by hand with its job
  skipped. The Render blueprint (`render.yaml`) is removed; delete the Render services (`flexstore-back`,
  `flexstore-db`) in the Render dashboard, and the Cloudflare Pages project when it is no longer needed.
- `CorsConfig` does not need `flexstore.tech`: the API is called from the same address.
