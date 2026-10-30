---
name: prompt-convention
description: How to turn the user's requests into changes - one kind of change per request (feature, refactoring, skill update), a request arriving while another is in progress, up to 10 requests carried in a row without waiting for their review, choices to validate rather than questions, a report at the end. Use whenever a request leads to code or skill changes, and when a new request arrives while another one is in progress. How each pull request is made and kept is in the pull-request-convention skill.
---

# Prompt conventions

Conventions collected from the user's feedback on how requests become changes. Follow them for every request. Each change becomes a pull request made and kept as the `pull-request-convention` skill says.

## 1. One request, one kind of change

Each request for a new feature gets its own pull request. A pull request does not mix kinds of change:
- a **feature** (new behaviour, its tests and its documentation);
- a **refactoring** (same behaviour, better code);
- a **skill update** (`.claude/skills/...`).

Why: a focused pull request is easier to review, answers one need, and can be merged, reverted or discussed on its own.

How to apply:
- **A new principle given while a feature is in progress**: the skill update is its own pull request; the code written for the current feature follows the principle right away; bringing the existing code in line with it is a separate refactoring pull request.
- **A request arriving while another is in progress**: finish or park the current work (commit on its branch), then handle the new request on its own branch.
- **If changes were mixed by mistake**, split them before opening the pull requests (one branch per kind) rather than asking the reviewer to untangle them.
- **A request too large for one pull request** is split into steps, each its own pull request.

## 2. Up to 10 requests in a row, reviewed later

The user may give several requests at once (features, refactorings), then come back later to review and merge the pull requests. Carry each request to its own open pull request, one after the other, without waiting for the review or the merge of the previous ones: **up to 10 open pull requests** at the same time.

Why: the user gives the instructions in one go and validates the pull requests when they come back, instead of waiting at each step.

How to apply:
- **At most 10 open pull requests** waiting for the review. Past that, stop and tell the user which ones wait for a merge before going on with the next requests.
- **Do not stop to ask between two requests**: a choice with a sensible default is made, and written in the description of the pull request under a **"Choices to validate"** heading, so the user can change it in the review. Only a request that cannot be done without the user's answer is left aside, with the question, and the other requests go on.
- **Each pull request is finished on its own** (see the `pull-request-convention` skill) before moving to the next request.
- **Report at the end**: one line per pull request, with its link, its kind (feature, refactoring, skill update), the order of merge when it matters, and the choices to validate.
