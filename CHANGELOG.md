# Changelog

## 0.0.2 — Unreleased

### License and distribution

- License project code and artwork under MIT, retaining the NeoForge template's original notice.
- Declare MIT in the mod metadata and include both notices in the mod and sources JARs.

### Optional server support

- Allow clients to join Create servers without Better Networking, and servers to accept clients without it.
- Add client-only clipboard network selection, clearing, nearby highlights and block counts using synchronized data.
- Keep local selections in session memory and clear them on disconnect without changing clipboard item data.
- Automatically enable full clipboard tools when both sides negotiate the existing optional payloads.
- Guard packet sends and server clipboard interactions for peers without the mod.
- Preserve Create's gauge placement rules when the client has no server support.
- Clear stale highlight counts and suppress duplicate Create link and panel outlines.
- Add client-only controls and server-support messages with English text in all 137 locale files.

### Repository preparation

- Limit the repository to production source, runtime resources, required build files, notices and documentation.
- Resolve the pinned Create 6.0.10 dependency from a public repository instead of requiring a local JAR.
- Produce an installable JAR and a production sources JAR with reproducible archive ordering and timestamps.
- Add usage, architecture, addon integration and build verification guides.
- Preserve earlier tests and optional automation in a separate local archive.

This preparation retains version 0.0.2 and does not change gameplay code or shipped resource bytes.
