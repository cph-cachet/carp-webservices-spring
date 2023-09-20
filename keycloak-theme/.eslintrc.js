module.exports = {
 parserOptions: {
    project: './tsconfig.json',
    sourceType: 'module',
    ecmaFeatures: {
      jsx: true,
    },
    ecmaVersion: 2020,
    tsconfigRootDir: __dirname,
  },
  extends: '@cph-cachet'
};
