export const AI_PAGE_GUTTER = 0
export const AI_PANEL_MIN_HEIGHT = 720
export const AI_SHELL_HEIGHT = 'calc(100vh - 92px)'

export const buildAiLayoutVars = () => ({
  '--ai-page-gutter': `${AI_PAGE_GUTTER}px`,
  '--ai-panel-min-height': `${AI_PANEL_MIN_HEIGHT}px`,
  '--ai-shell-height': AI_SHELL_HEIGHT,
})
