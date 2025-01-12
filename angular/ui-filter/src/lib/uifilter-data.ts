import { FilterMetadata } from "primeng/api";

/**
 * Represents data for filtering a data set.
 * @group Interface
 */
export class UIFilterData {
  /**
   * The value used for filtering.
   */
  public value?: any;

  /**
   * The match mode for filtering.
   */
  public matchMode?: string;

  /**
   * The operator for filtering.
   */
  public operator?: string;

  constructor(md: FilterMetadata) {
    this.value = md.value;
    this.operator = md.operator;
    var matchMode = md.matchMode;
    if (matchMode == 'after' || matchMode == 'dateAfter') {
      matchMode = 'gt';
    } else if (matchMode == 'before' || matchMode == 'dateBefore') {
      matchMode = 'lt';
    } else if (matchMode == 'is' || matchMode == 'dateIs') {
      matchMode = 'equals';
    } else if (matchMode == 'isNot' || matchMode == 'dateIsNot') {
      matchMode = 'notEquals';
    }
    this.matchMode = matchMode;
  }
}
