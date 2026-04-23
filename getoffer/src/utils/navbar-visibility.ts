export const NAVBAR_THRESHOLD = 240

export interface NavbarVisibilityState {
  isFixed: boolean
  isVisible: boolean
}

export const createNavbarVisibilityState = (): NavbarVisibilityState => ({
  isFixed: false,
  isVisible: false,
})

export const getNavbarPhase = (
  scrollY: number,
  threshold = NAVBAR_THRESHOLD,
): 'fixed' | 'static' => (scrollY > threshold ? 'fixed' : 'static')
