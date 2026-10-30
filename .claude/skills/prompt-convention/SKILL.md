---
name: prompt-convention
description: How to turn the user's requests into pull requests - one focused PR per request, never mixing features, refactorings and skill updates; up to 10 PRs carried in a row, without waiting for their review. Use whenever a request leads to code or skill changes, before creating a branch, committing or opening a PR, and when a new request arrives while another one is in progress.
---

# Prompt conventions

Conventions collected from the user's feedback on how requests become changes. Follow them for every request.

## 1. One pull request per request, never mixed

Each request for a new feature gets its own pull request. A pull request does not mix kinds of change:
- a **feature** (new behaviour, its tests and its documentation);
- a **refactoring** (same behaviour, better code);
- a **skill update** (`.claude/skills/...`).

Why: a focused pull request is easier to review, answers one need, and can be merged, reverted or discussed on its own.

How to apply:
- **Start each request from `main`** on its own branch (`feature/...`, `refactor/...`, `chore/...-skill`).
- **A PR that depends on another one** (e.g. a feature that needs a refactoring first): do the dependency in its own PR, then build the dependent branch on it, but open the dependent PR with **`main` as base**, never the branch of another PR. Say in the description which PR to merge first. Once the dependency is merged, rebase the dependent PR on `main` (see the `pull-request-convention` skill), so that it only shows its own changes.
- **A new principle given while a feature is in progress**: the skill update is its own PR; the code written for the current feature follows the principle right away; bringing the existing code in line with it is a separate refactoring PR.
- **A request arriving while another is in progress**: finish or park the current work (commit on its branch), then handle the new request on its own branch.
- **If changes were mixed by mistake**, split them before opening the PRs (one branch per kind, each from `main` or from the PR it depends on) rather than asking the reviewer to untangle them.
- The PR title and description say what the PR is (feature, refactoring, skill update) and, for a refactoring, that the behaviour does not change.

## 2. Every pull request targets `main`

A pull request never has the branch of another pull request as its base. When that base branch is merged first, the dependent PR is merged into a branch that is already merged, and its changes never reach `main`; when the dependent PR is merged first, `main` gets them only through the base PR.

After each merge on `main`, bring the open PRs up to date with it (rebase them on `main`, see the `pull-request-convention` skill), adapt them to what changed (a renamed type, a new API), and run all the tests: a PR that was green alone can break `main` once another one is merged before it. Check the CI of `main` after each merge.

## 3. Push the pull request as soon as the unit tests are green

Open (or update) the pull request as soon as the code compiles and the unit tests pass, before running the integration tests: the user can start the review earlier.

How to apply:
- unit tests green (`./gradlew test`, `npm test`, lint and type check) → push and open the PR, saying in the test plan that the integration tests are running;
- then run the integration tests (`./gradlew integrationTest`) and the manual checks;
- if they reveal a problem, fix it and push to the same PR, then update its description (test plan, and what changed when it matters for the review);
- once everything is green, tick the integration tests in the test plan.

## 4. Up to 10 pull requests in a row, reviewed later

The user may give several requests at once (features, refactorings), then come back later to review and merge the pull requests. Carry each request to its own open pull request, one after the other, without waiting for the review or the merge of the previous ones: **up to 10 open pull requests** at the same time.

Why: the user gives the instructions in one go and validates the pull requests when they come back, instead of waiting at each step.

How to apply:
- **One pull request per feature or refactoring**, each different from the others, each on its own branch started from `main` and targeting `main` (sections 1 and 2). A request too large for one pull request is split into steps, each its own pull request, and counted as such.
- **At most 10 open pull requests** waiting for the review. Past that, stop and tell the user which ones wait for a merge before going on with the next requests.
- **Do not stop to ask between two pull requests**: a choice with a sensible default is made, and written in the description of the pull request under a **"Choices to validate"** heading, so the user can change it in the review. Only a request that cannot be done without the user's answer is left aside, with the question, and the other requests go on.
- **Keep the pull requests independent**: avoid changing the same files in two pull requests when possible. When one really needs another, build the dependent one on it, keep `main` as the base, say in the description which pull request to merge first, and rebase it on `main` once that one is merged.
- **Migrations**: each pull request takes the next version number free among `main` and the other open pull requests (V25, V26...); if another one is merged first with the same number, renumber the migration and update the physical data model.
- **Switch branches cleanly**: commit the work of a branch before leaving it, never carry uncommitted changes from one pull request to another (a git worktree per branch is an option).
- **Each pull request is finished on its own**: unit tests green and pushed, then integration tests and manual checks ticked in its test plan (section 3), before moving to the next one.
- **Report at the end**: one line per pull request, with its link, its kind (feature, refactoring, skill update), the order of merge when it matters, and the choices to validate.
- After each merge, section 2 applies to every open pull request: rebase it on `main`, adapt it and run the tests again.
