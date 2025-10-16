# ⚡ VibeCode Short Meta Prompt

Use `docs/PROJECT_MAP.md` to locate all relevant files.  
Before any change, **create a short task execution plan** (`docs/TASK_PLANS/{task_id}_{task_name}.md`), outlining scope, dependencies, affected docs, and expected output.  
Then update necessary documentation, and only after that, perform the task implementation and update progress logs.

---

### 💡 Usage Example

To execute a specific task, mention this prompt and replace the placeholders:

```
[Short Meta Prompt]

Execute task T-006: Implement caching layer for nearby post queries in Ktor backend.
```

The AI tool will first generate the corresponding task execution plan under `docs/TASK_PLANS/`, update related docs, and then implement the changes step-by-step.
