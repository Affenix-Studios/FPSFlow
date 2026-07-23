# Contributing

Contributions to FPSFlow are welcome! This guide will help you get started.

## How to Contribute

### Reporting Bugs

1. Check [existing issues](https://github.com/Affenix-Studios/FPSFlow/issues) to avoid duplicates
2. Create a new issue with:
   - Clear title and description
   - Minecraft version, Fabric Loader version, Java version
   - List of installed mods
   - FPSFlow config (`config/fpsflow.json`)
   - Game log (`latest.log`)
   - Screenshots or video if applicable

### Suggesting Features

1. Check [existing issues](https://github.com/Affenix-Studios/FPSFlow/issues) for similar suggestions
2. Create a new issue with the `enhancement` label
3. Describe the feature and why it would be useful
4. Consider performance implications

### Submitting Pull Requests

1. Fork the repository
2. Create a feature branch from `main`
3. Make your changes
4. Test thoroughly in-game
5. Commit with a clear message
6. Push to your fork
7. Open a Pull Request

## Development Setup

See [Building from Source](Building-from-Source.md) for setup instructions.

## Code Guidelines

### General Principles

- Keep changes focused and minimal
- Maintain backward compatibility with existing configs
- Do not break existing features
- Test on both MC 1.21.11 and 26.2 if possible

### Java Code

- Follow the existing code style
- Use meaningful variable and method names
- Add Javadoc for public APIs
- Keep methods short and focused
- Avoid unnecessary allocations in hot paths

### Mixins

- Keep mixins minimal; inject only what is necessary
- Use `require = 0` for all mixins to ensure fail-soft behavior
- Comment why the mixin is needed
- Test with multiple Minecraft versions if possible

### Lithium-Inspired Code

- Reimplement concepts from scratch; do not copy-paste
- Add Javadoc `@see` references to the Lithium repository
- Attribute inspiration in comments
- Ensure compatibility with FPSFlow's architecture

### Configuration

- Add new config fields to `FPSFlowConfig.java`
- Provide sensible defaults
- Update the config screen if user-facing
- Document in wiki/Configuration.md

## Testing Checklist

Before submitting a PR:

- [ ] Code compiles without errors
- [ ] Code compiles without warnings (except deprecation warnings from dependencies)
- [ ] Feature works as intended in-game
- [ ] No crashes on launch
- [ ] No crashes during gameplay
- [ ] Config migration works (old configs still load)
- [ ] Compatibility with common mods (Sodium, EntityCulling, ImmediatelyFast)
- [ ] Debug log shows expected initialization messages

## Areas That Need Help

- Additional Lithium-inspired optimizations
- Performance profiling and benchmarking
- Documentation and wiki improvements
- Translation files (`en_us.json`, `de_de.json`)
- Testing on different Minecraft versions and mod loaders

## Questions?

Open an issue with the `question` label or join the [GitHub Discussions](https://github.com/Affenix-Studios/FPSFlow/discussions).

## License

FPSFlow is licensed under the **GNU Affero General Public License v3.0 (AGPL-3.0)**.

- You are free to use, modify, and distribute FPSFlow
- If you run a modified version on a server, you must provide the source code to users
- See [LICENSE](https://github.com/Affenix-Studios/FPSFlow/blob/main/LICENSE) for full terms