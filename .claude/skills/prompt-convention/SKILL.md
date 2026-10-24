---
name: prompt-convention
description: How to turn the user's requests into pull requests - one focused PR per request, never mixing features, refactorings and skill updates. Use whenever a request leads to code or skill changes, before creating a branch, committing or opening a PR, and when a new request arrives while another one is in progress.
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
- **A PR that depends on another one** (e.g. a feature that needs a refactoring first): do the dependency in its own PR, then merge its branch into the dependent branch, but open the dependent PR with **`main` as base**, never the branch of another PR. Say in the description which PR to merge first. Once the dependency is merged, the dependent PR only shows its own changes.
- **A new principle given while a feature is in progress**: the skill update is its own PR; the code written for the current feature follows the principle right away; bringing the existing code in line with it is a separate refactoring PR.
- **A request arriving while another is in progress**: finish or park the current work (commit on its branch), then handle the new request on its own branch.
- **If changes were mixed by mistake**, split them before opening the PRs (one branch per kind, each from `main` or from the PR it depends on) rather than asking the reviewer to untangle them.
- The PR title and description say what the PR is (feature, refactoring, skill update) and, for a refactoring, that the behaviour does not change.

## 2. Every pull request targets `main`

A pull request never has the branch of another pull request as its base. When that base branch is merged first, the dependent PR is merged into a branch that is already merged, and its changes never reach `main`; when the dependent PR is merged first, `main` gets them only through the base PR.

After each merge on `main`, bring the open PRs up to date with it (merge `main` into their branch, or rebase while nobody reviews them), adapt them to what changed (a renamed type, a new API), and run all the tests: a PR that was green alone can break `main` once another one is merged before it. Check the CI of `main` after each merge.

## 3. Push the pull request as soon as the unit tests are green

Open (or update) the pull request as soon as the code compiles and the unit tests pass, before running the integration tests: the user can start the review earlier.

How to apply:
- unit tests green (`./gradlew test`, `npm test`, lint and type check) → push and open the PR, saying in the test plan that the integration tests are running;
- then run the integration tests (`./gradlew integrationTest`) and the manual checks;
- if they reveal a problem, fix it and push to the same PR, then update its description (test plan, and what changed when it matters for the review);
- once everything is green, tick the integration tests in the test plan.
