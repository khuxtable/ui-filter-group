/**
 * Represents data for sorting.
 * @group Interface
 */
export class UIFilterSort {
	public field: string;
	public order: number;

	constructor(field: string, order: number) {
		this.field = field;
		this.order = order;
	}
}
