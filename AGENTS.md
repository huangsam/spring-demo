# Agent Behavior Tips

When doing refactoring or additive changes:
- Propose minimal edits
- Update tests and verify that `gradle build` passes

When giving performance or design advice:
- Warn about common pitfalls (e.g. N+1)
- Add comments explaining tradeoffs and reasoning behind choices

When writing new tests:
- Make sure that asc/desc tests check for ordering where relevant
- Adjust sort keys of test data to ensure deterministic ordering in tests
- Stub out external dependencies to reduce test runtime
