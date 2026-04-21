export const NAVBAR_THRESHOLD = 240

export const createNavbarVisibilityState = () => ({
  isFixed: false,
  isVisible: false,
})

export const getNavbarPhase = (scrollY, threshold = NAVBAR_THRESHOLD) =>
  scrollY > threshold ? 'fixed' : 'static'

