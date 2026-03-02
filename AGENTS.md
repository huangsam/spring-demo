# Agent Behavior Tips

If the user requests refactoring or additions, then:
- Propose minimal edits
- Update tests and verify that `gradle build` passes

If the user wants performance or design advice, then:
- Warn about common pitfalls (e.g. N+1)
- Add comments explaining tradeoffs and reasoning behind choices
