package jp.skypencil.brainjack;

import java.io.IOException;

import jp.skypencil.brainjack.Command.END_LOOP;
import jp.skypencil.brainjack.Command.START_LOOP;
import jp.skypencil.brainjack.Command.*;

interface Visitor {

	void visit(START_LOOP start_LOOP);

	void visit(END_LOOP end_LOOP);

	void visit(ACCEPT accept) throws IOException;

	void visit(OUTPUT output) throws IOException;

	void visit(DECREMENT_DATA decrement_DATA);

	void visit(INCREMENT_DATA increment_DATA);

	void visit(DECREMENT_DATA_POINTER decrement_DATA_POINTER);

	void visit(INCREMENT_DATA_POINTER increment_DATA_POINTER);

}
