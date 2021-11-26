package at.researchstudio.sat.merkmalservice.ifc.parser;

import at.researchstudio.sat.merkmalservice.ifc.model.IfcLine;
import java.util.Objects;
import java.util.function.Function;

public class IfcLineParserImpl<T extends IfcLine> implements IfcLineParser<T> {
    private final Function<String, T> lineGenerator;

    public IfcLineParserImpl(Function<String, T> lineGenerator) {
        Objects.requireNonNull(lineGenerator);
        this.lineGenerator = lineGenerator;
    }

    @Override
    public T parse(String line) {
        return lineGenerator.apply(line);
    }
}
