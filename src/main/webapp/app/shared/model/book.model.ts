import { Moment } from 'moment';

export interface IBook {
  id?: number;
  title?: string;
  description?: string;
  publicationDate?: Moment;
  price?: number;
  authorId?: number;
}

export const defaultValue: Readonly<IBook> = {};
