import path from 'node:path'
import { fileURLToPath } from 'node:url'
import type { MarkdownRenderer } from 'vitepress'

// GitHub renders README.md as a directory index, so the guides link to README.md files. The site serves
// them under other names (see `rewrites`), and VitePress resolves relative links against the rewritten
// paths. Each README is rewritten within its own directory, so swapping the file name is enough.
export const readmeRewrites: Record<string, string> = {
  'README.md': 'guide.md',
  'type-classes/README.md': 'type-classes/index.md',
}

const srcDir = fileURLToPath(new URL('..', import.meta.url))
const readmeLinkRE = /^((?:[^:#?]*\/)?README\.md)([#?].*)?$/

export function readmeLinksPlugin(md: MarkdownRenderer): void {
  md.core.ruler.after('inline', 'readme-links', (state) => {
    const source = path.relative(srcDir, state.env.realPath ?? state.env.path)
    state.tokens
      .flatMap((token) => token.children ?? [])
      .filter((token) => token.type === 'link_open')
      .forEach((token) => token.attrSet('href', rewriteHref(token.attrGet('href') ?? '', source)))
  })
}

function rewriteHref(href: string, source: string): string {
  const match = href.match(readmeLinkRE)
  if (!match) return href
  const [, file, suffix = ''] = match
  const target = readmeRewrites[path.posix.join(path.posix.dirname(source), file)]
  return target ? `${path.posix.join(path.posix.dirname(file), path.posix.basename(target))}${suffix}` : href
}
