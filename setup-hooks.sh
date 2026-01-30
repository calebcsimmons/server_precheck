#!/bin/bash

# Script to set up git hooks

echo "Setting up git hooks..."

# Make the pre-commit hook executable
chmod +x .git-hooks/pre-commit

# Create symlink from .git/hooks to our custom hooks
ln -sf ../../.git-hooks/pre-commit .git/hooks/pre-commit

echo "✅ Git hooks installed successfully!"
echo ""
echo "The pre-commit hook will now run Checkstyle before each commit."
echo "To bypass the hook (not recommended), use: git commit --no-verify"

