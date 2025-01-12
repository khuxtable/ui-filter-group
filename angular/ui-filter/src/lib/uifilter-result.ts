/**
 * Result contains the data and record count (for pagination)
 */
export interface UIFilterResult<T> {
	records: T[];
	totalRecords: number;
}