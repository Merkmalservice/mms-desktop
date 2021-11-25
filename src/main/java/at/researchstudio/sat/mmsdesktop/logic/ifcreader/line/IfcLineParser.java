package at.researchstudio.sat.mmsdesktop.logic.ifcreader.line;

import at.researchstudio.sat.mmsdesktop.model.ifc.IfcLine;

/**
 * Parses one line of an IFC File,
 */
public interface IfcLineParser<T extends IfcLine> {
    /**
     * Returns the appropriate IfcLine object for the specified String.
     * @param line a line of an IFC file
     * @return an object representing the parsed line
     */
    T parse(String line);
}
