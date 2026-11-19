import { defineConfig, type DefaultTheme } from 'vitepress'
import { readmeLinksPlugin, readmeRewrites } from './readme-links'

const repo = 'https://github.com/kotlinz/kotlinz'
const base = '/kotlinz/'

export default defineConfig({
  title: 'Kotlinz',
  description:
    'Functional programming for Kotlin: HKT-encoded type classes, data types, and category-theory constructs.',
  base,
  lang: 'en-US',
  cleanUrls: true,
  lastUpdated: true,
  appearance: 'dark',
  rewrites: readmeRewrites,
  markdown: { config: readmeLinksPlugin },
  // The API reference is copied into the build output by the docs workflow after the site is built.
  ignoreDeadLinks: [/^\/api(\/|$)/],
  head: [['link', { rel: 'icon', type: 'image/svg+xml', href: `${base}logo.svg` }]],
  themeConfig: {
    logo: '/logo.svg',
    nav: nav(),
    sidebar: sidebar(),
    search: { provider: 'local' },
    socialLinks: [{ icon: 'github', link: repo }],
    editLink: {
      pattern: `${repo}/edit/master/docs/:path`,
      text: 'Edit this page on GitHub',
    },
    footer: {
      message: 'Released under the Apache License 2.0.',
      copyright: 'Copyright © kotlinz contributors',
    },
    outline: { level: [2, 3] },
  },
})

function nav(): DefaultTheme.NavItem[] {
  return [
    { text: 'Guide', link: '/getting-started', activeMatch: '^/(?!index\\.md$)' },
    { text: 'API Reference', link: '/api/', target: '_self' },
  ]
}

function sidebar(): DefaultTheme.Sidebar {
  return [
    {
      text: 'Introduction',
      items: [
        { text: 'Getting Started', link: '/getting-started' },
        { text: 'Guides Overview', link: '/guide' },
        { text: 'Higher-kinded Types', link: '/hkt' },
      ],
    },
    {
      text: 'Type Classes',
      link: '/type-classes/',
      items: [
        { text: 'Functor, Applicative, Monad', link: '/type-classes/functor-applicative-monad' },
        { text: 'Selective', link: '/type-classes/selective' },
        { text: 'Foldable and Traversable', link: '/type-classes/foldable-traversable' },
        { text: 'Align and Filterable', link: '/type-classes/align-filterable' },
        { text: 'Errors and MTL', link: '/type-classes/mtl' },
        { text: 'Eq, Order, Show, Hash', link: '/type-classes/eq-order-show-hash' },
        { text: 'Contravariant Functors', link: '/type-classes/contravariant' },
        { text: 'Bifunctor, Profunctor, Arrow', link: '/type-classes/bifunctor-profunctor-arrow' },
        { text: 'Distributive, Representable', link: '/type-classes/distributive-representable' },
      ],
    },
    {
      text: 'Data Types and Effects',
      items: [
        { text: 'Data Types', link: '/data-types' },
        { text: 'Data Structures', link: '/data-structures' },
        { text: 'Effects', link: '/effects' },
        { text: 'Monad Comprehension (fx)', link: '/fx' },
        { text: 'Monad Transformers', link: '/transformers' },
        { text: 'Monoids and Algebra', link: '/monoids' },
      ],
    },
    {
      text: 'Advanced',
      items: [
        { text: 'Optics', link: '/optics' },
        { text: 'Comonads', link: '/comonads' },
        { text: 'Automata and Folds', link: '/automata' },
        { text: 'Category Theory', link: '/category-theory' },
      ],
    },
    {
      text: 'Reference',
      items: [{ text: 'API Reference', link: '/api/', target: '_self' }],
    },
  ]
}
