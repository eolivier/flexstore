---
name: pull-request-convention
description: How a pull request is kept up to date - every new or updated pull request is rebased on main, so that its diff only shows its own changes (no merge commit of main, no commit of another pull request). Use before pushing a branch, opening or updating a pull request, and after each merge on main.
---

# Pull request convention

Conventions collected from the user's feedback on how a pull request is kept, so that it can be reviewed. Follow them for every pull request; they complete the `prompt-convention` skill (one pull request per request, based on `main`).

## 1. A pull request is rebased on `main`: its diff only shows its own changes

Every new pull request, and every pull request updated after a merge on `main`, is **rebased on the current `main`**: its commits are replayed on top of `main`, without merge commit. The diff of the pull request then only shows the changes of that pull request, never a merge of `main` nor the commits of another pull request.

Why: the reviewer reads only what the pull request changes; a merge of `main`, or the commits of a pull request it was built on, hide its own changes among others that were already reviewed.

How to apply:
- **Before opening a pull request**: start the branch from the current `main` (update it first), and rebase it again if `main` moved before the push.
- **After each merge on `main`**: rebase every open pull request on the new `main`, run all the tests again (unit, integration, lint, build), then push it. Check the CI of `main` after the merge.
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
