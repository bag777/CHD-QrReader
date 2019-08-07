package com.chdc.qr;

import com.github.sarxos.webcam.WebcamResolution;
import org.pushingpixels.substance.api.SubstanceSkin;
import org.pushingpixels.substance.api.skin.*;

import java.util.Arrays;
import java.util.List;

public interface QRResources {
    String DRIVER_CLASS_NAME = "org.h2.Driver";
    String DB_URL = "jdbc:h2:~/QRCodeReader;AUTO_SERVER=TRUE";
    String DB_USER = "chdc";
    String DB_PASSWORD = "";

    WebcamResolution Resolution = WebcamResolution.HD;

    String SelectedDir = "UserSelectedDir";

    enum FinanceType {
        Basic(1, "일반계정", Arrays.asList(100101, 100102, 100303, 100304, 100305)),
        Mission(2, "선교계정", Arrays.asList(100101));

        int code;
        String name;
        List<Integer> labelTargetList;

        FinanceType(int code, String name, List<Integer> labelTargetList) {
            this.code = code;
            this.name = name;
            this.labelTargetList = labelTargetList;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public static FinanceType getByCode(int code) {
            for (FinanceType type : values()) {
                if (type.getCode() == code) {
                    return type;
                }
            }
            return null;
        }

        public static FinanceType getByName(String name) {
            for (FinanceType type : values()) {
                if (type.getName().equals(name)) {
                    return type;
                }
            }
            return null;
        }

        public boolean isTarget(int code) {
            return this.labelTargetList.contains(code);
        }
    }

    enum Mode {
        Ready,
        Insert,
        Update,
        Delete;
    }

    enum Skin {
        AutumnSkin("Autumn", new AutumnSkin()),
        BusinessSkin("Business", new BusinessSkin()),
        BusinessBlackSteelSkin("Business Black Steel", new BusinessBlackSteelSkin()),
        BusinessBlueSteelSkin("Business Blue Steel", new BusinessBlueSteelSkin()),
        CremeSkin("Creme", new CremeSkin()),
        CremeCoffeeSkin("Creme Coffee", new CremeCoffeeSkin()),
        DustSkin("Dust", new DustSkin()),
        DustCoffeeSkin("Dust Coffee", new DustCoffeeSkin()),
        GeminiSkin("Gemini", new GeminiSkin()),
        MarinerSkin("Mariner", new MarinerSkin()),
        ModerateSkin("Moderate", new ModerateSkin()),
        NebulaSkin("Nebula", new NebulaSkin()),
        NebulaBrickWallSkin("Nebula Brick Wall", new NebulaBrickWallSkin()),
        OfficeBlack2007Skin("Office Black 2007", new OfficeBlack2007Skin()),
        OfficeSilver2007Skin("Office Silver 2007", new OfficeSilver2007Skin()),
        OfficeBlue2007Skin("Office Blue 2007", new OfficeBlue2007Skin()),
        SaharaSkin("Sahara", new SaharaSkin()),
        MagellanSkin("Magellan", new MagellanSkin()),
        ChallengerDeepSkin("Challenger Deep", new ChallengerDeepSkin()),
        EmeraldDuskSkin("Emerald Dusk", new EmeraldDuskSkin()),
        RavenSkin("Raven", new RavenSkin()),
        GraphiteSkin("Graphite", new GraphiteSkin()),
        GraphiteGlassSkin("Graphite Glass", new GraphiteGlassSkin()),
        GraphiteAquaSkin("Graphite Aqua", new GraphiteAquaSkin()),
        TwilightSkin("Twilight", new TwilightSkin());

        String visible;
        SubstanceSkin skin;

        Skin(String visible, SubstanceSkin skin) {
            this.visible = visible;
            this.skin = skin;
        }

        public String getVisible() {
            return visible;
        }

        public SubstanceSkin getSkin() {
            return skin;
        }
    }
}
