export const ARTICLE_LIST_PAGE_SIZE = 5
export const FAVORITES_PAGE_SIZE = 5
export const PROFILE_ARTICLES_PAGE_SIZE = 6

type QueryParams = Record<string, unknown>

export const parsePage = (value: unknown, fallback = 1): number => {
  const page = Number.parseInt(String(value ?? ''), 10)
  return Number.isInteger(page) && page > 0 ? page : fallback
}

export const withPageInQuery = (query: QueryParams = {}, page = 1): QueryParams => {
  const nextQuery: QueryParams = { ...query }

  if (page > 1) {
    nextQuery.page = String(page)
  } else {
    delete nextQuery.page
  }

  return nextQuery
}

export const resetPageInQuery = (query: QueryParams = {}): QueryParams => withPageInQuery(query, 1)
