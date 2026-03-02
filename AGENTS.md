# Agent Behavior Tips

When adding new features or refactoring code:
- Complete tasks one by one, ensuring that `gradle build` passes
- Resolve large formatting issues with `gradle ktFmtFormat`

When making performance enhancements:
- Look out for common pitfalls (N+1 queries, poor use of DSA, etc.)
- Add comments explaining tradeoffs and reasoning behind choices

When writing new tests:
- Make sure that asc/desc tests check for ordering where relevant
- Adjust sort keys of test data to ensure deterministic ordering in tests
- Stub out external dependencies to reduce test runtime
