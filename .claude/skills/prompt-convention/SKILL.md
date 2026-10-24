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
- **A feature that needs a refactoring first**: do the refactoring in its own PR, then base the feature branch on it and open the feature PR with the refactoring branch as base, so that its diff shows the feature only. Say in the description which PR to merge first.
- **A new principle given while a feature is in progress**: the skill update is its own PR; the code written for the current feature follows the principle right away; bringing the existing code in line with it is a separate refactoring PR.
- **A request arriving while another is in progress**: finish or park the current work (commit on its branch), then handle the new request on its own branch.
- **If changes were mixed by mistake**, split them before opening the PRs (one branch per kind, each from `main` or from the PR it depends on) rather than asking the reviewer to untangle them.
- The PR title and description say what the PR is (feature, refactoring, skill update) and, for a refactoring, that the behaviour does not change.
