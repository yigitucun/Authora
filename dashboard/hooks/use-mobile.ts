import * as React from "react"

const MOBILE_BREAKPOINT = 768

export function useIsMobile() {
  const getSnapshot = () => {
    if (typeof window === "undefined") {
      return false
    }
    return window.innerWidth < MOBILE_BREAKPOINT
  }

  const subscribe = (callback: () => void) => {
    if (typeof window === "undefined") {
      return () => undefined
    }
    const mql = window.matchMedia(`(max-width: ${MOBILE_BREAKPOINT - 1}px)`)
    const onChange = () => callback()
    mql.addEventListener("change", onChange)
    return () => mql.removeEventListener("change", onChange)
  }

  return React.useSyncExternalStore(subscribe, getSnapshot, () => false)
}
