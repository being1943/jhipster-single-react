import axios from 'axios';
import {
  parseHeaderForLinks,
  loadMoreDataWhenScrolled,
  ICrudGetAction,
  ICrudGetAllAction,
  ICrudPutAction,
  ICrudDeleteAction
} from 'react-jhipster';

import { cleanEntity } from 'app/shared/util/entity-utils';
import { REQUEST, SUCCESS, FAILURE } from 'app/shared/reducers/action-type.util';

import { IAuthor, defaultValue } from 'app/shared/model/author.model';

export const ACTION_TYPES = {
  FETCH_AUTHOR_LIST: 'author/FETCH_AUTHOR_LIST',
  FETCH_AUTHOR: 'author/FETCH_AUTHOR',
  CREATE_AUTHOR: 'author/CREATE_AUTHOR',
  UPDATE_AUTHOR: 'author/UPDATE_AUTHOR',
  DELETE_AUTHOR: 'author/DELETE_AUTHOR',
  RESET: 'author/RESET'
};

const initialState = {
  loading: false,
  errorMessage: null,
  entities: [] as ReadonlyArray<IAuthor>,
  entity: defaultValue,
  links: { next: 0 },
  updating: false,
  totalItems: 0,
  updateSuccess: false
};

export type AuthorState = Readonly<typeof initialState>;

// Reducer

export default (state: AuthorState = initialState, action): AuthorState => {
  switch (action.type) {
    case REQUEST(ACTION_TYPES.FETCH_AUTHOR_LIST):
    case REQUEST(ACTION_TYPES.FETCH_AUTHOR):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        loading: true
      };
    case REQUEST(ACTION_TYPES.CREATE_AUTHOR):
    case REQUEST(ACTION_TYPES.UPDATE_AUTHOR):
    case REQUEST(ACTION_TYPES.DELETE_AUTHOR):
      return {
        ...state,
        errorMessage: null,
        updateSuccess: false,
        updating: true
      };
    case FAILURE(ACTION_TYPES.FETCH_AUTHOR_LIST):
    case FAILURE(ACTION_TYPES.FETCH_AUTHOR):
    case FAILURE(ACTION_TYPES.CREATE_AUTHOR):
    case FAILURE(ACTION_TYPES.UPDATE_AUTHOR):
    case FAILURE(ACTION_TYPES.DELETE_AUTHOR):
      return {
        ...state,
        loading: false,
        updating: false,
        updateSuccess: false,
        errorMessage: action.payload
      };
    case SUCCESS(ACTION_TYPES.FETCH_AUTHOR_LIST):
      const links = parseHeaderForLinks(action.payload.headers.link);
      return {
        ...state,
        links,
        loading: false,
        totalItems: action.payload.headers['x-total-count'],
        entities: loadMoreDataWhenScrolled(state.entities, action.payload.data, links)
      };
    case SUCCESS(ACTION_TYPES.FETCH_AUTHOR):
      return {
        ...state,
        loading: false,
        entity: action.payload.data
      };
    case SUCCESS(ACTION_TYPES.CREATE_AUTHOR):
    case SUCCESS(ACTION_TYPES.UPDATE_AUTHOR):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: action.payload.data
      };
    case SUCCESS(ACTION_TYPES.DELETE_AUTHOR):
      return {
        ...state,
        updating: false,
        updateSuccess: true,
        entity: {}
      };
    case ACTION_TYPES.RESET:
      return {
        ...initialState
      };
    default:
      return state;
  }
};

const apiUrl = 'api/authors';

// Actions

export const getEntities: ICrudGetAllAction<IAuthor> = (page, size, sort) => {
  const requestUrl = `${apiUrl}${sort ? `?page=${page}&size=${size}&sort=${sort}` : ''}`;
  return {
    type: ACTION_TYPES.FETCH_AUTHOR_LIST,
    payload: axios.get<IAuthor>(`${requestUrl}${sort ? '&' : '?'}cacheBuster=${new Date().getTime()}`)
  };
};

export const getEntity: ICrudGetAction<IAuthor> = id => {
  const requestUrl = `${apiUrl}/${id}`;
  return {
    type: ACTION_TYPES.FETCH_AUTHOR,
    payload: axios.get<IAuthor>(requestUrl)
  };
};

export const createEntity: ICrudPutAction<IAuthor> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.CREATE_AUTHOR,
    payload: axios.post(apiUrl, cleanEntity(entity))
  });
  return result;
};

export const updateEntity: ICrudPutAction<IAuthor> = entity => async dispatch => {
  const result = await dispatch({
    type: ACTION_TYPES.UPDATE_AUTHOR,
    payload: axios.put(apiUrl, cleanEntity(entity))
  });
  return result;
};

export const deleteEntity: ICrudDeleteAction<IAuthor> = id => async dispatch => {
  const requestUrl = `${apiUrl}/${id}`;
  const result = await dispatch({
    type: ACTION_TYPES.DELETE_AUTHOR,
    payload: axios.delete(requestUrl)
  });
  return result;
};

export const reset = () => ({
  type: ACTION_TYPES.RESET
});
