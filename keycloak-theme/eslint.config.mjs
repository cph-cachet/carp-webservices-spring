// eslint.config.js
import js from "@eslint/js";
import ts from "@typescript-eslint/eslint-plugin";
import parser from "@typescript-eslint/parser";
import reactRefresh from "eslint-plugin-react-refresh";
import storybook from "eslint-plugin-storybook";
// import config from "@carp-dk/eslint-config";

module.exports  = defineConfig([
  js.configs.recommended,
//   config, // Your custom ESLint config
  storybook.configs.recommended,
  {
    root: true,
    ignores: ["dist/", ".eslintrc"],
    languageOptions: {
      parser,
      parserOptions: {
        project: "./keycloak-theme/tsconfig.json",
        sourceType: "module",
      },
      globals: {
        browser: true,
        es2020: true,
      },
    },
    plugins: {
      "@typescript-eslint": ts,
      "react-refresh": reactRefresh,
    },
    rules: {
      "react-refresh/only-export-components": [
        "warn",
        { allowConstantExport: true },
      ],
      "react-hooks/exhaustive-deps": "off",
      "@typescript-eslint/no-redeclare": "off",
      "no-labels": "off",
    },
  },
  {
    files: ["**/*.stories.*"],
    rules: {
      "import/no-anonymous-default-export": "off",
    },
  },
]);
