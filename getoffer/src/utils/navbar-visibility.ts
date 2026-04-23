export const NAVBAR_THRESHOLD = 240

export type NavbarPhase = 'fixed' | 'static'

export type NavbarVisibilityState = {
  isFixed: boolean
  isVisible: boolean
}

export const createNavbarVisibilityState = (): NavbarVisibilityState => ({
  isFixed: false,
  isVisible: false,
})

export const getNavbarPhase = (scrollY: number, threshold = NAVBAR_THRESHOLD): NavbarPhase =>
  scrollY > threshold ? 'fixed' : 'static'
