package at.researchstudio.sat.merkmalservice.ifc.model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ReferenceRemoverTests {

    @Test
    public void test_lastList_firstElement() {
        String line =
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#490,#491,#492,#493));";
        Integer id = 236;
        Assertions.assertEquals(
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#490,#491,#492,#493));",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    public void test_lastList_innerElement() {
        String line =
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#490,#491,#492,#493));";
        Integer id = 490;
        Assertions.assertEquals(
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#491,#492,#493));",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    public void test_lastList_lastElement() {
        String line =
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#490,#491,#492,#493));";
        Integer id = 493;
        Assertions.assertEquals(
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#490,#491,#492));",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    public void test_lastList_onlyElement() {
        String line =
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236));";
        Integer id = 236;
        Assertions.assertEquals(
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,$);",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    public void test_innerList_firstElement() {
        String line =
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#490,#491,#492,#493),$);";
        Integer id = 236;
        Assertions.assertEquals(
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#490,#491,#492,#493),$);",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    public void test_innerList_innerElement() {
        String line =
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#490,#491,#492,#493),$);";
        Integer id = 490;
        Assertions.assertEquals(
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#491,#492,#493),$);",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    public void test_innerList_lastElement() {
        String line =
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#490,#491,#492,#493),$);";
        Integer id = 493;
        Assertions.assertEquals(
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#490,#491,#492),$);",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    public void test_innerList_onlyElement() {
        String line =
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236),$);";
        Integer id = 236;
        Assertions.assertEquals(
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,$,$);",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    public void test_innerElement() {
        String line =
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',#41,'Analytische Eigenschaften',$,(#236,#490,#491,#492,#493));";
        Integer id = 41;
        Assertions.assertEquals(
                "#541= IFCPROPERTYSET('0CUddw_UD3HAXeXivuznJL',$,'Analytische Eigenschaften',$,(#236,#490,#491,#492,#493));",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    public void test_possibleBug() {
        String line =
                "#505= IFCPROPERTYSET('0CUddw_UD3HAXeXeTuznIB',#41,'Abh\\X2\\00E4\\X0\\ngigkeiten',$,(#211,#212,#213,#215,#216,#217,#218,#219,#220,#221,#481));";
        Integer id = 216;
        Assertions.assertEquals(
                "#505= IFCPROPERTYSET('0CUddw_UD3HAXeXeTuznIB',#41,'Abh\\X2\\00E4\\X0\\ngigkeiten',$,(#211,#212,#213,#215,#217,#218,#219,#220,#221,#481));",
                ReferenceRemover.removeReferenceTo(line, id));
    }

    @Test
    void test_possibleBug2() {
        String line =
                "#476= IFCWALLTYPE('0CUddw_UD3HAXeW9DuznJL',#41,'Basiswand:CAx_200+_AW_NITR_BN_0,35',$,$,(#541),$,'435781',$,.STANDARD.);";
        String expected =
                "#476= IFCWALLTYPE('0CUddw_UD3HAXeW9DuznJL',#41,'Basiswand:CAx_200+_AW_NITR_BN_0,35',$,$,$,$,'435781',$,.STANDARD.);";
        Integer id = 541;
        Assertions.assertEquals(expected, ReferenceRemover.removeReferenceTo(line, id));
    }
}
