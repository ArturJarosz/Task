#!/bin/bash

echo "checking gh version"
gh --version

gh api repos/ArturJarosz/Task/releases

exit 0
