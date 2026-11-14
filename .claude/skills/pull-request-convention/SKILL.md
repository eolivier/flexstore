---
name: pull-request-convention
description: How a pull request is made and kept - a branch from main targeting main (a dependent pull request included), every pull request rebased on main so that its diff only shows its own changes, pushed as soon as the code compiles, opened ready for review (never as a draft), its title (Conventional Commits: feat, fix, refactor, chore...) and description (written in English), independent pull requests and migration numbers, switching branches cleanly, comments handled right away with the oldest pull request first (written in English and in French, clear and concise; skills checked before asking a question), every open pull request rebased after each merge. Use before creating a branch, committing, pushing, opening or updating a pull request, when a comment is added to a pull request, and after each merge on main.
---

# Pull request convention

Conventions collected from the user's feedback on how a pull request is made and kept, so that it can be reviewed. Follow them for every pull request. Which requests become which pull requests is in the `prompt-convention` skill.

## 1. A branch from `main`, a pull request targeting `main`

Each pull request starts from the current `main`, on its own branch named after its kind (`feature/...`, `refactor/...`, `chore/...-skill`, `fix/...`), and has `main` as its base. A pull request never has the branch of another pull request as its base: when that base branch is merged first, the dependent pull request is merged into a branch that is already merged, and its changes never reach `main`; when the dependent pull request is merged first, `main` gets them only through the base pull request.

How to apply:
- **A pull request that depends on another one** (e.g. a feature that needs a refactoring first): the dependency is its own pull request; the dependent branch is built on it, but the dependent pull request is opened with `main` as its base. Its description says which pull request to merge first. Once the dependency is merged, the dependent pull request is rebased on `main` (section 2).
- **Before pushing to an existing branch**, check that its pull request is still open (`gh pr view <number> --json state`): commits pushed to a merged branch never reach `main`; a follow-up is a new branch from `main`.
- **The title follows Conventional Commits**: `<type>: <summary>`, or `<type>(<scope>): <summary>` when a scope helps (`backend`, `front`, `skills`, `db`...). The type is the kind of change, in lowercase:
  - `feat`: a new feature for the user (a use case, a page, an endpoint);
  - `fix`: a bug fix;
  - `refactor`: a change of code structure or naming, the behaviour does not change;
  - `chore`: maintenance that touches neither the behaviour nor the structure of the product (dependencies, configuration, skill update);
  - `docs`: documentation only (`README.md`, `docs/database.md`);
  - `test`: tests only;
  - `perf`: a performance improvement;
  - `build` / `ci`: the build (Gradle, npm) / the CI workflows;
  - `style`: formatting only (line length, whitespace).

  The summary is in the imperative mood, in English, starts with a lowercase letter, has no final period, and the whole title stays at 72 characters at most (e.g. `feat(backend): add the order status history`, `refactor: move the domain model into sub-packages`, `chore(skills): name pull requests after conventional commits`). A breaking change adds `!` after the type (`feat(api)!: ...`). One pull request has one type, which is the one of its branch prefix (section 1: `feature/` → `feat`, `refactor/` → `refactor`, `chore/` → `chore`, `fix/` → `fix`); a change that needs two types is two pull requests (see the `prompt-convention` skill).
- **The description** says what the pull request is and, for a refactoring, that the behaviour does not change; the choices made without the user are under a "Choices to validate" heading (see the `prompt-convention` skill).
- **Opened ready for review**: a pull request is opened as ready for review, never as a draft (no `--draft` flag with `gh pr create`), so that the reviewer can start right away. Section 3 says when to open it; its test plan tells what is still running.
- **Written in English**: the title, the description (summary, choices to validate, test plan) and its later updates are written in English, whatever the language of the request or of the conversation; only the texts quoted from the product (labels, messages, invoice mentions) keep their own language.

## 2. A pull request is rebased on `main`: its diff only shows its own changes

Every new pull request, and every pull request updated after a merge on `main`, is **rebased on the current `main`**: its commits are replayed on top of `main`, without merge commit. The diff of the pull request then only shows the changes of that pull request, never a merge of `main` nor the commits of another pull request.

Why: the reviewer reads only what the pull request changes; a merge of `main`, or the commits of a pull request it was built on, hide its own changes among others that were already reviewed.

How to apply:
- **Before opening a pull request**: start the branch from the current `main` (update it first), and rebase it again if `main` moved before the push.
- **After each merge on `main`**: rebase every open pull request on the new `main` (section 5 gives the order), adapt it to what changed (a renamed type, a new API), run all the tests again (unit, integration, lint, build), then push it: a pull request that was green alone can break `main` once another one is merged before it. Check the CI of `main` after the merge.
- **A pull request that needs another one** (still open): it cannot show only its own changes until the other one is merged. Say in its description which pull request to merge first; as soon as that one is merged, rebase it on `main`, so that its diff only shows its own changes again.
- **One commit or a few clear commits**: the "work in progress" commits and the fixes of the review may be squashed into the commit they complete when rebasing.
- **Check the content**: after the rebase, the tree of the branch is the one expected (same files as before, plus `main`); when the branch already contained `main`, `git diff <old head>` must be empty for the files of `main`.
- **Push with a lease**: a rebased branch is pushed with `git push --force-with-lease=refs/heads/<branch>:<head pushed before>`, never with a plain `--force`, so that a commit pushed meanwhile by someone else is not lost. Check before that the remote head is still the one pushed before (`git ls-remote`).
- **Never rebase `main`**, nor a branch already merged: a merged pull request is closed; its follow-up is a new branch from `main`.

Example, after a merge on `main`:

```bash
git checkout main && git pull
git checkout feature/order-status-history
old=$(git rev-parse HEAD)
git rebase main                      # resolve the conflicts, if any, then: git rebase --continue
./gradlew checkLineLength test integrationTest && (cd front && npm run lint && npm test)
git push --force-with-lease="refs/heads/feature/order-status-history:${old}" origin feature/order-status-history
```

## 3. Push the pull request as soon as the code compiles

Push the branch and open the pull request as soon as the code compiles, before running any test: the user can start the review earlier, and follows the tests as they come. The tests then run on the pushed pull request, and its description is updated with what they say.

How to apply:
- the code compiles (`./gradlew compileKotlin compileTestKotlin`, `npm run build`, type check) → push and open the pull request, saying in the test plan that the unit tests and the integration tests are not green yet;
- then run the unit tests (`./gradlew checkLineLength test`, `npm test`, lint), and tick them in the test plan once they are green;
- then run the integration tests (`./gradlew integrationTest`) and the manual checks;
- **if a test fails**, fix it and push to the same pull request, then update its description (test plan, and what changed when it matters for the review); never leave a pull request with a red test unexplained;
- once everything is green, tick the integration tests in the test plan.

A box of the test plan is ticked only when that check has run green on the last commit of the branch.

A pull request is finished on its own (unit tests, integration tests and manual checks ticked) before moving to the next one.

## 4. Several open pull requests side by side

When several pull requests are open at the same time (see the `prompt-convention` skill):
- **Keep them independent**: avoid changing the same files in two pull requests when possible; when one really needs another, follow section 1.
- **Migrations**: each pull request takes the next version number free among `main` and the other open pull requests (V25, V26...); if another one is merged first with the same number, renumber the migration and update the physical data model.
- **Switch branches cleanly**: commit the work of a branch before leaving it, never carry uncommitted changes from one pull request to another (a git worktree per branch is an option).

## 5. Comments first, oldest pull request first, rebase after each merge

The goal is to merge the pull requests as fast as possible, the oldest first.

- **A comment is handled first**: as soon as a comment is added to a pull request (review comment, general comment, requested change), it takes priority over any other work, a new request included: stop what is in progress (commit it first, section 4), answer or fix, push, and reply on the comment.
- **The older the pull request, the higher the priority**: when comments are waiting on several pull requests, handle them in the order of creation of the pull requests, the oldest first, so that the oldest one can be merged first. A comment on a recent pull request waits until the older comments are handled.
- **Comments are bilingual, clear and concise**: every comment written on a pull request (reply to a review comment, general comment, question) has two sections, an **English** one and a **Français** one, the second being the translation of the first (same content, nothing added or left out). Each section is short and direct: what was done or what is asked, no filler. This does not change the title and the description of the pull request, which stay in English only (section 1). Example:

  ```
  **English**: Fixed, the label now comes from the i18n file.

  **Français** : Corrigé, le libellé vient maintenant du fichier i18n.
  ```
- **Points to validate: check the skills first**: before asking the user a question or writing a comment that asks for a validation, read the skills that apply (`.claude/skills/`, starting with `prompt-convention` and this one) and `CLAUDE.md`. If a skill already answers, apply it without asking; if a sensible default exists, take it and list it under "Choices to validate" (see the `prompt-convention` skill). Only a point that no skill settles and that blocks the work is asked, in the bilingual format above.
- **After each merge on `main`, rebase the other open pull requests** on the new `main`, so that they include what the merge changed on `main`: do it right away, oldest pull request first, following section 2 (adapt, run all the tests, push with a lease, check the CI). The merged pull request is never touched again.

Example, after a merge on `main`, listing the open pull requests from the oldest to the newest:

```bash
gh pr list --state open --json number,headRefName,createdAt --jq 'sort_by(.createdAt)[] | "\(.number) \(.headRefName)"'
```
