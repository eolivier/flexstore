// @ts-check

import js from "@eslint/js";
import tseslint from "typescript-eslint";
import vue from "eslint-plugin-vue";
import prettier from "eslint-config-prettier";
import unusedImports from "eslint-plugin-unused-imports";

export default [
    // Recos JS et TS
    js.configs.recommended,
    ...tseslint.configs.recommended,

    // Preset Vue pour flat config
    ...vue.configs["flat/recommended"],

    // Associer le parser TS à <script lang="ts"> dans les .vue
    {
        files: ["**/*.vue"],
        languageOptions: {
            parserOptions: {
                // fait parser le <script> par typescript-eslint
                parser: tseslint.parser,
                ecmaVersion: "latest",
                sourceType: "module",
                extraFileExtensions: [".vue"],
            },
        },
    },

    // Règles communes + unused vars/imports via TS
    {
        files: ["**/*.{js,ts,vue}"],
        plugins: {
            "unused-imports": unusedImports,
        },
        rules: {
            "no-unused-vars": "off",
            'vue/multi-word-component-names': 'off',
            'vue/valid-v-slot': 'off',
            "@typescript-eslint/no-unused-vars": [
                "warn",
                {
                    vars: "all",
                    args: "after-used",
                    ignoreRestSiblings: true,
                    argsIgnorePattern: "^_",
                    varsIgnorePattern: "^_",
                },
            ],
            "unused-imports/no-unused-imports": "error",
        },
    },

    {
        ignores: [
            'node_modules/',
            'dist/',
            'build/',
            'public/',
        ],
        // ...autres options de config
    },

    // Désactive les règles en conflit avec Prettier (à garder en dernier)
    prettier,
];
