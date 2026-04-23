export const ARTICLE_LIST_PAGE_SIZE = 5
export const FAVORITES_PAGE_SIZE = 5
export const PROFILE_ARTICLES_PAGE_SIZE = 6

export const parsePage = (value?: string | number, fallback = 1) => {
  const page = Number.parseInt(String(value ?? ''), 10)
  return Number.isInteger(page) && page > 0 ? page : fallback
}

export const withPageInQuery = (query: Record<string, string> = {}, page = 1) => {
  const nextQuery = { ...query }

  if (page > 1) {
    nextQuery.page = String(page)
  } else {
    delete nextQuery.page
  }

  return nextQuery
}

export const resetPageInQuery = (query: Record<string, string> = {}) => withPageInQuery(query, 1)
