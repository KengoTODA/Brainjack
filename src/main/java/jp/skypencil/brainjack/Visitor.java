package jp.skypencil.brainjack;

import java.io.IOException;

import jp.skypencil.brainjack.Command.EndLoop;
import jp.skypencil.brainjack.Command.StartLoop;
import jp.skypencil.brainjack.Command.*;

interface Visitor {

	void visit(StartLoop start_LOOP);

	void visit(EndLoop end_LOOP);

	void visit(Accept accept) throws IOException;

	void visit(Output output) throws IOException;

	void visit(DecrementData decrement_DATA);

	void visit(IncrementData increment_DATA);

	void visit(DecrementDataPointer decrement_DATA_POINTER);

	void visit(IncrementDataPointer increment_DATA_POINTER);

}
