import { Moment } from 'moment';
import { IBook } from 'app/shared/model/book.model';

export interface IAuthor {
  id?: number;
  name?: string;
  birthDate?: Moment;
  books?: IBook[];
}

export const defaultValue: Readonly<IAuthor> = {};
