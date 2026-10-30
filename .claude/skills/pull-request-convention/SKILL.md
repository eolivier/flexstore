---
name: pull-request-convention
description: How a pull request is made and kept - a branch from main targeting main (a dependent pull request included), every pull request rebased on main so that its diff only shows its own changes, pushed as soon as the unit tests are green, its title and description, independent pull requests and migration numbers, switching branches cleanly. Use before creating a branch, committing, pushing, opening or updating a pull request, and after each merge on main.
---

# Pull request convention

Conventions collected from the user's feedback on how a pull request is made and kept, so that it can be reviewed. Follow them for every pull request. Which requests become which pull requests is in the `prompt-convention` skill.

## 1. A branch from `main`, a pull request targeting `main`

Each pull request starts from the current `main`, on its own branch named after its kind (`feature/...`, `refactor/...`, `chore/...-skill`, `fix/...`), and has `main` as its base. A pull request never has the branch of another pull request as its base: when that base branch is merged first, the dependent pull request is merged into a branch that is already merged, and its changes never reach `main`; when the dependent pull request is merged first, `main` gets them only through the base pull request.

How to apply:
- **A pull request that depends on another one** (e.g. a feature that needs a refactoring first): the dependency is its own pull request; the dependent branch is built on it, but the dependent pull request is opened with `main` as its base. Its description says which pull request to merge first. Once the dependency is merged, the dependent pull request is rebased on `main` (section 2).
- **Before pushing to an existing branch**, check that its pull request is still open (`gh pr view <number> --json state`): commits pushed to a merged branch never reach `main`; a follow-up is a new branch from `main`.
- **The title and the description** say what the pull request is (feature, refactoring, skill update, fix) and, for a refactoring, that the behaviour does not change; the choices made without the user are under a "Choices to validate" heading (see the `prompt-convention` skill).

## 2. A pull request is rebased on `main`: its diff only shows its own changes

Every new pull request, and every pull request updated after a merge on `main`, is **rebased on the current `main`**: its commits are replayed on top of `main`, without merge commit. The diff of the pull request then only shows the changes of that pull request, never a merge of `main` nor the commits of another pull request.

Why: the reviewer reads only what the pull request changes; a merge of `main`, or the commits of a pull request it was built on, hide its own changes among others that were already reviewed.

How to apply:
- **Before opening a pull request**: start the branch from the current `main` (update it first), and rebase it again if `main` moved before the push.
- **After each merge on `main`**: rebase every open pull request on the new `main`, adapt it to what changed (a renamed type, a new API), run all the tests again (unit, integration, lint, build), then push it: a pull request that was green alone can break `main` once another one is merged before it. Check the CI of `main` after the merge.
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

## 3. Push the pull request as soon as the unit tests are green

Open (or update) the pull request as soon as the code compiles and the unit tests pass, before running the integration tests: the user can start the review earlier.

How to apply:
- unit tests green (`./gradlew checkLineLength test`, `npm test`, lint and type check) → push and open the pull request, saying in the test plan that the integration tests are running;
- then run the integration tests (`./gradlew integrationTest`) and the manual checks;
- if they reveal a problem, fix it and push to the same pull request, then update its description (test plan, and what changed when it matters for the review);
- once everything is green, tick the integration tests in the test plan.

A pull request is finished on its own (unit tests, integration tests and manual checks ticked) before moving to the next one.

## 4. Several open pull requests side by side

When several pull requests are open at the same time (see the `prompt-convention` skill):
- **Keep them independent**: avoid changing the same files in two pull requests when possible; when one really needs another, follow section 1.
- **Migrations**: each pull request takes the next version number free among `main` and the other open pull requests (V25, V26...); if another one is merged first with the same number, renumber the migration and update the physical data model.
- **Switch branches cleanly**: commit the work of a branch before leaving it, never carry uncommitted changes from one pull request to another (a git worktree per branch is an option).

