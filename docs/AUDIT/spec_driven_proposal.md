# Spec-Driven Development Proposal for NearYou ID

**Date:** 2026-02-07
**Status:** Proposal

---

## 1. What is OpenSpec?

[OpenSpec](https://github.com/Fission-AI/OpenSpec) is a lightweight spec-driven development (SDD) framework for AI coding assistants. It uses a two-folder model:

- `openspec/specs/` -- current system specifications (source of truth)
- `openspec/changes/` -- proposed changes being worked on

Each change gets its own folder containing:
- `proposal.md` -- rationale and scope
- `specs/` -- requirements and usage scenarios
- `design.md` -- technical approach
- `tasks.md` -- implementation checklist

Key commands: `/opsx:new`, `/opsx:ff`, `/opsx:apply`, `/opsx:archive`.

OpenSpec works with Claude Code, Cursor, Codex, and other AI tools. It is free and open source.

---

## 2. Current State of NearYou ID Documentation

### Strengths
- Detailed API documentation (`API_DOCUMENTATION.md`) with request/response examples
- Clear architecture document (`ARCHITECTURE.md`) covering MVI, clean architecture, KMP
- Product specification (`SPEC.md`) with feature requirements
- Shared Kotlin models (`shared/src/commonMain/kotlin/domain/model/`) serve as the single source of truth for DTOs

### Gaps
- **No machine-readable API contracts** -- API docs are prose-based markdown, not parseable specs
- **No change tracking** -- When a feature is added, there is no structured record of the proposal, design decisions, or task breakdown
- **Documentation drift risk** -- As the codebase grows, markdown docs may fall out of sync with actual implementation
- **No spec-to-test linkage** -- Test cases are not traceable back to specification requirements

---

## 3. Prioritized Spec List

Create specs in this order, matching existing implementation maturity:

| Priority | Spec Name | Rationale |
|----------|-----------|-----------|
| 1 | `auth-api` | Fully implemented; establishes the spec pattern with a stable feature |
| 2 | `post-api` | Fully implemented with PostGIS; validates geo-query spec patterns |
| 3 | `user-profile-api` | Implemented; covers CRUD + photo upload |
| 4 | `shared-models` | Documents the KMP shared model contracts that bind frontend and backend |
| 5 | `messaging-api` | Not yet implemented; first spec-first feature |
| 6 | `subscription-api` | Not yet implemented; spec-first |
| 7 | `search-api` | Not yet implemented; spec-first (premium feature) |

---

## 4. Example Spec: Nearby Posts API

Below is a concrete example of what an OpenSpec spec file would look like for the nearby posts endpoint.

### File: `openspec/specs/post-api/spec.md`

```markdown
# Post API Specification

## Overview
Location-based post creation and retrieval for the NearYou ID platform.

## Data Models

### Post (shared: `domain.model.Post`)
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| id | String (UUID) | yes | Server-generated |
| userId | String (UUID) | yes | Author reference |
| user | UserSummary | yes | Embedded author info |
| content | String | yes | 1-500 chars |
| location | Location | yes | lat/lon pair |
| mediaUrls | List<String> | no | Premium only, max 4 |
| likeCount | Int | yes | Default 0 |
| commentCount | Int | yes | Default 0 |
| isLikedByCurrentUser | Boolean | yes | Default false |
| distance | Double? | no | Meters from requester |
| createdAt | Instant | yes | ISO 8601 |
| updatedAt | Instant | yes | ISO 8601 |

### CreatePostRequest (shared: `domain.model.CreatePostRequest`)
| Field | Type | Required | Notes |
|-------|------|----------|-------|
| content | String | yes | 1-500 chars |
| location | Location | yes | |
| mediaUrls | List<String> | no | Premium only |

## Endpoints

### GET /posts/nearby
- **Auth:** Required (JWT)
- **Query params:** lat (float), lon (float), radius (enum: 1000|5000|10000|20000), limit (int, 1-100, default 20)
- **Response 200:** `{ posts: Post[] }`
- **Errors:** 400 INVALID_RADIUS, 400 INVALID_COORDINATES, 401 INVALID_TOKEN
- **Implementation:** PostGIS ST_DWithin with geography cast, ordered by distance ASC

### POST /posts
- **Auth:** Required (JWT)
- **Body:** CreatePostRequest
- **Response 201:** Post
- **Errors:** 400 INVALID_CONTENT, 403 PREMIUM_REQUIRED (if mediaUrls and free tier)
- **Business rules:** Free users limited to 100 posts/day

### GET /posts/:id
- **Auth:** Required (JWT)
- **Response 200:** Post
- **Errors:** 404 POST_NOT_FOUND

### PUT /posts/:id
- **Auth:** Required (JWT, owner only)
- **Body:** UpdatePostRequest
- **Response 200:** Post
- **Errors:** 403 AUTHORIZATION_ERROR, 404 POST_NOT_FOUND

### DELETE /posts/:id
- **Auth:** Required (JWT, owner only)
- **Response:** 204 No Content
- **Errors:** 403 AUTHORIZATION_ERROR, 404 POST_NOT_FOUND
- **Implementation:** Soft delete (isDeleted = true)

## Validation Rules
- Content: 1-500 characters, non-blank
- Location: latitude -90..90, longitude -180..180
- Radius: must be one of [1000, 5000, 10000, 20000]
- MediaUrls: max 4 items, valid URL format, JPEG/PNG/WebP only
```

---

## 5. Incremental Adoption Plan

### Phase 1: Retrofit existing features (Week 1-2)
1. Install OpenSpec: `npm install -g openspec`
2. Create `openspec/specs/` for the 3 implemented features (auth, posts, user-profile)
3. Write specs that match current implementation exactly -- no changes to code
4. Validate specs against existing tests

### Phase 2: Spec-first for new features (Week 3+)
1. For the next feature (messaging), start with `/opsx:new messaging-api`
2. Write proposal.md, specs, and design.md before any code
3. Use `/opsx:apply` to implement from spec
4. Archive completed changes with `/opsx:archive`

### Phase 3: Shared model alignment (Ongoing)
1. Create a `shared-models` spec that documents all `domain.model.*` classes
2. When a spec changes a model, the shared Kotlin model is the single source of truth
3. OpenSpec specs serve as the human-readable contract; Kotlin `@Serializable` classes remain the machine-enforceable contract

---

## 6. How Shared Specs Align Frontend and Backend

NearYou ID already has a strong foundation: the `shared/` KMP module defines all DTOs once and both `composeApp/` and `server/` import them. OpenSpec complements this by:

| Concern | Current (KMP shared) | With OpenSpec |
|---------|----------------------|---------------|
| Type safety | Kotlin compiler enforces | Same (unchanged) |
| Human-readable contract | API_DOCUMENTATION.md (manual) | OpenSpec specs (structured, versioned) |
| Change proposals | Ad-hoc PRs | Structured proposal.md + design.md |
| Task breakdown | GitHub issues | tasks.md co-located with spec |
| AI assistant context | Must read multiple files | Single spec folder per feature |
| Audit trail | Git history | openspec/changes/archive/ |

The key insight: **OpenSpec does not replace the shared KMP module**. It adds a structured documentation layer on top. The Kotlin models remain the single source of truth for serialization. OpenSpec specs are the single source of truth for *intent and design decisions*.

---

## 7. Migration Path (Incremental and Reversible)

### Reversibility guarantees
- OpenSpec is documentation-only; it does not generate code or modify build files
- Removing OpenSpec means deleting the `openspec/` directory -- zero impact on builds
- Existing docs in `docs/CORE/` remain untouched; specs are additive

### Directory structure after adoption
```
NearYouID/
  openspec/
    specs/
      auth-api/
        spec.md
      post-api/
        spec.md
      user-profile-api/
        spec.md
      shared-models/
        spec.md
    changes/
      messaging-api/        # in-progress feature
        proposal.md
        specs/
        design.md
        tasks.md
    archive/
      2026-02-messaging-api/ # completed features
  docs/CORE/                 # existing docs (unchanged)
  shared/                    # KMP shared models (unchanged)
  server/                    # backend (unchanged)
  composeApp/                # frontend (unchanged)
```

### Rollback
If the team decides OpenSpec is not valuable after Phase 1:
1. Delete `openspec/` directory
2. No code changes needed
3. Existing `docs/CORE/` documentation continues to serve its purpose

---

## 8. Recommendations

1. **Start small**: Retrofit `auth-api` spec first as a proof-of-concept
2. **Do not duplicate**: Specs reference shared Kotlin model class names rather than re-defining types
3. **Keep specs lean**: Focus on endpoints, business rules, and error codes -- not implementation details
4. **Use for AI context**: When prompting AI coding assistants, point them at the relevant `openspec/specs/` folder
5. **Review specs in PRs**: Add `openspec/` changes to PR review checklist
