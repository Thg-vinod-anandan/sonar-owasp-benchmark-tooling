#!/bin/bash

set -euo pipefail

function installTravisTools {
  mkdir ~/.local
  curl -sSL https://github.com/SonarSource/travis-utils/tarball/7be522bebb2b1b78493db0b2b1dd8e4e38e58bf4 | tar zx --strip-components 1 -C ~/.local
  source ~/.local/bin/install
}

installTravisTools

regular_mvn_build_deploy_analyze -P\!coverage-per-test
