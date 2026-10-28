package train.render.models;

import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.ModelRendererTurbo;

/**
 * A luxury sleeping car of the early 1900s, made for this port (written by tools/gen_lux_sleeper.py, with its
 * texture): navy and gold, a clerestory roof, a corridor along one side, four sleeping compartments (a velvet sofa,
 * the upper berth folded up, a table by the window, a washstand with a mirror), vestibules with doors at both ends.
 * x along, y up, z across, in pixels.
 */
public class ModelPassengerLuxSleeper extends ModelBase {
    static final int TEX_W = 2048, TEX_H = 512;

    public ModelPassengerLuxSleeper() {
        bodyModel = new ModelRendererTurbo[436];

        bodyModel[0] = new ModelRendererTurbo(this, 1082, 210, TEX_W, TEX_H); // wheel
        bodyModel[0].addBox(-86F, 0F, -11F, 12F, 12F, 2F, 0, 0.5F);
        bodyModel[1] = new ModelRendererTurbo(this, 1111, 210, TEX_W, TEX_H); // wheel
        bodyModel[1].addBox(-86F, 0F, 9F, 12F, 12F, 2F, 0, 0.5F);
        bodyModel[2] = new ModelRendererTurbo(this, 1697, 184, TEX_W, TEX_H); // axle
        bodyModel[2].addBox(-81F, 5F, -9F, 2F, 2F, 18F, 0, 0.5F);
        bodyModel[3] = new ModelRendererTurbo(this, 1140, 210, TEX_W, TEX_H); // wheel
        bodyModel[3].addBox(-62F, 0F, -11F, 12F, 12F, 2F, 0, 0.5F);
        bodyModel[4] = new ModelRendererTurbo(this, 1169, 210, TEX_W, TEX_H); // wheel
        bodyModel[4].addBox(-62F, 0F, 9F, 12F, 12F, 2F, 0, 0.5F);
        bodyModel[5] = new ModelRendererTurbo(this, 1738, 184, TEX_W, TEX_H); // axle
        bodyModel[5].addBox(-57F, 5F, -9F, 2F, 2F, 18F, 0, 0.5F);
        bodyModel[6] = new ModelRendererTurbo(this, 1198, 242, TEX_W, TEX_H); // bogie side frame
        bodyModel[6].addBox(-90F, 4F, -14F, 44F, 6F, 2F, 0, 0.5F);
        bodyModel[7] = new ModelRendererTurbo(this, 1291, 242, TEX_W, TEX_H); // axlebox
        bodyModel[7].addBox(-83F, 3F, -15F, 6F, 6F, 2F, 0, 0.5F);
        bodyModel[8] = new ModelRendererTurbo(this, 1308, 242, TEX_W, TEX_H); // axlebox
        bodyModel[8].addBox(-59F, 3F, -15F, 6F, 6F, 2F, 0, 0.5F);
        bodyModel[9] = new ModelRendererTurbo(this, 992, 255, TEX_W, TEX_H); // bolster spring
        bodyModel[9].addBox(-74F, 6F, -15.2F, 12F, 4F, 2F, 0, 0.5F);
        bodyModel[10] = new ModelRendererTurbo(this, 1325, 242, TEX_W, TEX_H); // bogie side frame
        bodyModel[10].addBox(-90F, 4F, 12F, 44F, 6F, 2F, 0, 0.5F);
        bodyModel[11] = new ModelRendererTurbo(this, 1418, 242, TEX_W, TEX_H); // axlebox
        bodyModel[11].addBox(-83F, 3F, 13F, 6F, 6F, 2F, 0, 0.5F);
        bodyModel[12] = new ModelRendererTurbo(this, 1435, 242, TEX_W, TEX_H); // axlebox
        bodyModel[12].addBox(-59F, 3F, 13F, 6F, 6F, 2F, 0, 0.5F);
        bodyModel[13] = new ModelRendererTurbo(this, 1021, 255, TEX_W, TEX_H); // bolster spring
        bodyModel[13].addBox(-74F, 6F, 13.2F, 12F, 4F, 2F, 0, 0.5F);
        bodyModel[14] = new ModelRendererTurbo(this, 1138, 112, TEX_W, TEX_H); // bolster
        bodyModel[14].addBox(-72F, 10F, -14F, 8F, 4F, 28F, 0, 0.5F);
        bodyModel[15] = new ModelRendererTurbo(this, 1198, 210, TEX_W, TEX_H); // wheel
        bodyModel[15].addBox(50F, 0F, -11F, 12F, 12F, 2F, 0, 0.5F);
        bodyModel[16] = new ModelRendererTurbo(this, 1227, 210, TEX_W, TEX_H); // wheel
        bodyModel[16].addBox(50F, 0F, 9F, 12F, 12F, 2F, 0, 0.5F);
        bodyModel[17] = new ModelRendererTurbo(this, 1779, 184, TEX_W, TEX_H); // axle
        bodyModel[17].addBox(55F, 5F, -9F, 2F, 2F, 18F, 0, 0.5F);
        bodyModel[18] = new ModelRendererTurbo(this, 1256, 210, TEX_W, TEX_H); // wheel
        bodyModel[18].addBox(74F, 0F, -11F, 12F, 12F, 2F, 0, 0.5F);
        bodyModel[19] = new ModelRendererTurbo(this, 1285, 210, TEX_W, TEX_H); // wheel
        bodyModel[19].addBox(74F, 0F, 9F, 12F, 12F, 2F, 0, 0.5F);
        bodyModel[20] = new ModelRendererTurbo(this, 1820, 184, TEX_W, TEX_H); // axle
        bodyModel[20].addBox(79F, 5F, -9F, 2F, 2F, 18F, 0, 0.5F);
        bodyModel[21] = new ModelRendererTurbo(this, 1452, 242, TEX_W, TEX_H); // bogie side frame
        bodyModel[21].addBox(46F, 4F, -14F, 44F, 6F, 2F, 0, 0.5F);
        bodyModel[22] = new ModelRendererTurbo(this, 1545, 242, TEX_W, TEX_H); // axlebox
        bodyModel[22].addBox(53F, 3F, -15F, 6F, 6F, 2F, 0, 0.5F);
        bodyModel[23] = new ModelRendererTurbo(this, 1562, 242, TEX_W, TEX_H); // axlebox
        bodyModel[23].addBox(77F, 3F, -15F, 6F, 6F, 2F, 0, 0.5F);
        bodyModel[24] = new ModelRendererTurbo(this, 1050, 255, TEX_W, TEX_H); // bolster spring
        bodyModel[24].addBox(62F, 6F, -15.2F, 12F, 4F, 2F, 0, 0.5F);
        bodyModel[25] = new ModelRendererTurbo(this, 1579, 242, TEX_W, TEX_H); // bogie side frame
        bodyModel[25].addBox(46F, 4F, 12F, 44F, 6F, 2F, 0, 0.5F);
        bodyModel[26] = new ModelRendererTurbo(this, 1672, 242, TEX_W, TEX_H); // axlebox
        bodyModel[26].addBox(53F, 3F, 13F, 6F, 6F, 2F, 0, 0.5F);
        bodyModel[27] = new ModelRendererTurbo(this, 1689, 242, TEX_W, TEX_H); // axlebox
        bodyModel[27].addBox(77F, 3F, 13F, 6F, 6F, 2F, 0, 0.5F);
        bodyModel[28] = new ModelRendererTurbo(this, 1079, 255, TEX_W, TEX_H); // bolster spring
        bodyModel[28].addBox(62F, 6F, 13.2F, 12F, 4F, 2F, 0, 0.5F);
        bodyModel[29] = new ModelRendererTurbo(this, 1211, 112, TEX_W, TEX_H); // bolster
        bodyModel[29].addBox(64F, 10F, -14F, 8F, 4F, 28F, 0, 0.5F);
        bodyModel[30] = new ModelRendererTurbo(this, 501, 112, TEX_W, TEX_H); // underframe
        bodyModel[30].addBox(-104F, 12F, -18F, 208F, 2F, 36F, 0, 0.5F);
        bodyModel[31] = new ModelRendererTurbo(this, 1108, 255, TEX_W, TEX_H); // solebar
        bodyModel[31].addBox(-104F, 12F, -21F, 208F, 4F, 2F, 0, 0.5F);
        bodyModel[32] = new ModelRendererTurbo(this, 1298, 264, TEX_W, TEX_H); // footboard
        bodyModel[32].addBox(-80F, 10F, -24F, 160F, 1F, 3F, 0, 0.5F);
        bodyModel[33] = new ModelRendererTurbo(this, 62, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[33].addBox(-73F, 11F, -22.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[34] = new ModelRendererTurbo(this, 69, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[34].addBox(-49F, 11F, -22.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[35] = new ModelRendererTurbo(this, 76, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[35].addBox(-25F, 11F, -22.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[36] = new ModelRendererTurbo(this, 83, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[36].addBox(-1F, 11F, -22.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[37] = new ModelRendererTurbo(this, 90, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[37].addBox(23F, 11F, -22.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[38] = new ModelRendererTurbo(this, 97, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[38].addBox(47F, 11F, -22.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[39] = new ModelRendererTurbo(this, 104, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[39].addBox(71F, 11F, -22.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[40] = new ModelRendererTurbo(this, 1529, 255, TEX_W, TEX_H); // solebar
        bodyModel[40].addBox(-104F, 12F, 19F, 208F, 4F, 2F, 0, 0.5F);
        bodyModel[41] = new ModelRendererTurbo(this, 1625, 264, TEX_W, TEX_H); // footboard
        bodyModel[41].addBox(-80F, 10F, 21F, 160F, 1F, 3F, 0, 0.5F);
        bodyModel[42] = new ModelRendererTurbo(this, 111, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[42].addBox(-73F, 11F, 21.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[43] = new ModelRendererTurbo(this, 118, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[43].addBox(-49F, 11F, 21.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[44] = new ModelRendererTurbo(this, 125, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[44].addBox(-25F, 11F, 21.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[45] = new ModelRendererTurbo(this, 132, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[45].addBox(-1F, 11F, 21.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[46] = new ModelRendererTurbo(this, 139, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[46].addBox(23F, 11F, 21.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[47] = new ModelRendererTurbo(this, 146, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[47].addBox(47F, 11F, 21.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[48] = new ModelRendererTurbo(this, 153, 271, TEX_W, TEX_H); // footboard bracket
        bodyModel[48].addBox(71F, 11F, 21.5F, 2F, 2F, 1F, 0, 0.5F);
        bodyModel[49] = new ModelRendererTurbo(this, 729, 152, TEX_W, TEX_H); // battery box
        bodyModel[49].addBox(-28F, 6F, -12F, 16F, 6F, 24F, 0, 0.5F);
        bodyModel[50] = new ModelRendererTurbo(this, 0, 210, TEX_W, TEX_H); // gas tank
        bodyModel[50].addBox(-52F, 8F, -6F, 16F, 4F, 12F, 0, 0.5F);
        bodyModel[51] = new ModelRendererTurbo(this, 433, 0, TEX_W, TEX_H); // buffer beam
        bodyModel[51].addBox(-106F, 12F, -22F, 2F, 6F, 44F, 0, 0.5F);
        bodyModel[52] = new ModelRendererTurbo(this, 1950, 255, TEX_W, TEX_H); // buffer
        bodyModel[52].addBox(-110F, 13.5F, -15.5F, 4F, 3F, 3F, 0, 0.5F);
        bodyModel[53] = new ModelRendererTurbo(this, 1070, 242, TEX_W, TEX_H); // buffer head
        bodyModel[53].addBox(-111F, 12.5F, -16.5F, 1F, 5F, 5F, 0, 0.5F);
        bodyModel[54] = new ModelRendererTurbo(this, 1965, 255, TEX_W, TEX_H); // buffer
        bodyModel[54].addBox(-110F, 13.5F, 12.5F, 4F, 3F, 3F, 0, 0.5F);
        bodyModel[55] = new ModelRendererTurbo(this, 1083, 242, TEX_W, TEX_H); // buffer head
        bodyModel[55].addBox(-111F, 12.5F, 11.5F, 1F, 5F, 5F, 0, 0.5F);
        bodyModel[56] = new ModelRendererTurbo(this, 1952, 264, TEX_W, TEX_H); // coupling hook
        bodyModel[56].addBox(-108F, 14F, -1F, 2F, 2F, 2F, 0, 0.5F);
        bodyModel[57] = new ModelRendererTurbo(this, 1050, 264, TEX_W, TEX_H); // step
        bodyModel[57].addBox(-102F, 9F, -25F, 12F, 1F, 4F, 0, 0.5F);
        bodyModel[58] = new ModelRendererTurbo(this, 1961, 264, TEX_W, TEX_H); // step
        bodyModel[58].addBox(-102F, 14F, -24F, 12F, 1F, 3F, 0, 0.5F);
        bodyModel[59] = new ModelRendererTurbo(this, 1144, 184, TEX_W, TEX_H); // grab handle
        bodyModel[59].addBox(-90F, 24F, -23F, 1F, 20F, 1F, 0, 0.5F);
        bodyModel[60] = new ModelRendererTurbo(this, 1083, 264, TEX_W, TEX_H); // step
        bodyModel[60].addBox(-102F, 9F, 21F, 12F, 1F, 4F, 0, 0.5F);
        bodyModel[61] = new ModelRendererTurbo(this, 1992, 264, TEX_W, TEX_H); // step
        bodyModel[61].addBox(-102F, 14F, 21F, 12F, 1F, 3F, 0, 0.5F);
        bodyModel[62] = new ModelRendererTurbo(this, 1149, 184, TEX_W, TEX_H); // grab handle
        bodyModel[62].addBox(-90F, 24F, 22F, 1F, 20F, 1F, 0, 0.5F);
        bodyModel[63] = new ModelRendererTurbo(this, 810, 152, TEX_W, TEX_H); // battery box
        bodyModel[63].addBox(12F, 6F, -12F, 16F, 6F, 24F, 0, 0.5F);
        bodyModel[64] = new ModelRendererTurbo(this, 57, 210, TEX_W, TEX_H); // gas tank
        bodyModel[64].addBox(36F, 8F, -6F, 16F, 4F, 12F, 0, 0.5F);
        bodyModel[65] = new ModelRendererTurbo(this, 526, 0, TEX_W, TEX_H); // buffer beam
        bodyModel[65].addBox(104F, 12F, -22F, 2F, 6F, 44F, 0, 0.5F);
        bodyModel[66] = new ModelRendererTurbo(this, 1980, 255, TEX_W, TEX_H); // buffer
        bodyModel[66].addBox(106F, 13.5F, -15.5F, 4F, 3F, 3F, 0, 0.5F);
        bodyModel[67] = new ModelRendererTurbo(this, 1096, 242, TEX_W, TEX_H); // buffer head
        bodyModel[67].addBox(110F, 12.5F, -16.5F, 1F, 5F, 5F, 0, 0.5F);
        bodyModel[68] = new ModelRendererTurbo(this, 1995, 255, TEX_W, TEX_H); // buffer
        bodyModel[68].addBox(106F, 13.5F, 12.5F, 4F, 3F, 3F, 0, 0.5F);
        bodyModel[69] = new ModelRendererTurbo(this, 1109, 242, TEX_W, TEX_H); // buffer head
        bodyModel[69].addBox(110F, 12.5F, 11.5F, 1F, 5F, 5F, 0, 0.5F);
        bodyModel[70] = new ModelRendererTurbo(this, 2023, 264, TEX_W, TEX_H); // coupling hook
        bodyModel[70].addBox(106F, 14F, -1F, 2F, 2F, 2F, 0, 0.5F);
        bodyModel[71] = new ModelRendererTurbo(this, 1116, 264, TEX_W, TEX_H); // step
        bodyModel[71].addBox(90F, 9F, -25F, 12F, 1F, 4F, 0, 0.5F);
        bodyModel[72] = new ModelRendererTurbo(this, 0, 271, TEX_W, TEX_H); // step
        bodyModel[72].addBox(90F, 14F, -24F, 12F, 1F, 3F, 0, 0.5F);
        bodyModel[73] = new ModelRendererTurbo(this, 1154, 184, TEX_W, TEX_H); // grab handle
        bodyModel[73].addBox(89F, 24F, -23F, 1F, 20F, 1F, 0, 0.5F);
        bodyModel[74] = new ModelRendererTurbo(this, 1149, 264, TEX_W, TEX_H); // step
        bodyModel[74].addBox(90F, 9F, 21F, 12F, 1F, 4F, 0, 0.5F);
        bodyModel[75] = new ModelRendererTurbo(this, 31, 271, TEX_W, TEX_H); // step
        bodyModel[75].addBox(90F, 14F, 21F, 12F, 1F, 3F, 0, 0.5F);
        bodyModel[76] = new ModelRendererTurbo(this, 1159, 184, TEX_W, TEX_H); // grab handle
        bodyModel[76].addBox(89F, 24F, 22F, 1F, 20F, 1F, 0, 0.5F);
        bodyModel[77] = new ModelRendererTurbo(this, 0, 264, TEX_W, TEX_H); // floor edge
        bodyModel[77].addBox(-104F, 16F, -22F, 208F, 4F, 2F, 0, 0.5F);
        bodyModel[78] = new ModelRendererTurbo(this, 421, 264, TEX_W, TEX_H); // floor edge
        bodyModel[78].addBox(-104F, 16F, 20F, 208F, 4F, 2F, 0, 0.5F);
        bodyModel[79] = new ModelRendererTurbo(this, 513, 66, TEX_W, TEX_H); // carpet
        bodyModel[79].addBox(-88F, 16F, -20F, 176F, 4F, 40F, 0, 0.5F);
        bodyModel[80] = new ModelRendererTurbo(this, 946, 66, TEX_W, TEX_H); // vestibule floor
        bodyModel[80].addBox(-104F, 16F, -20F, 16F, 4F, 40F, 0, 0.5F);
        bodyModel[81] = new ModelRendererTurbo(this, 1059, 66, TEX_W, TEX_H); // vestibule floor
        bodyModel[81].addBox(88F, 16F, -20F, 16F, 4F, 40F, 0, 0.5F);
        bodyModel[82] = new ModelRendererTurbo(this, 18, 227, TEX_W, TEX_H);
        bodyModel[82].addBox(-104F, 20F, 21F, 208F, 12F, 1F, 0, 0.5F);
        bodyModel[83] = new ModelRendererTurbo(this, 0, 242, TEX_W, TEX_H);
        bodyModel[83].addBox(-104F, 46F, 21F, 208F, 11F, 1F, 0, 0.5F);
        bodyModel[84] = new ModelRendererTurbo(this, 160, 271, TEX_W, TEX_H);
        bodyModel[84].addBox(-104F, 57F, 21F, 208F, 2F, 1F, 0, 0.5F);
        bodyModel[85] = new ModelRendererTurbo(this, 1538, 271, TEX_W, TEX_H); // waist rail
        bodyModel[85].addBox(-104F, 30.5F, 22F, 208F, 1.5F, 0.6F, 0, 0.5F);
        bodyModel[86] = new ModelRendererTurbo(this, 0, 276, TEX_W, TEX_H); // cant rail
        bodyModel[86].addBox(-104F, 56.5F, 22F, 208F, 1F, 0.6F, 0, 0.5F);
        bodyModel[87] = new ModelRendererTurbo(this, 419, 276, TEX_W, TEX_H); // bottom rail
        bodyModel[87].addBox(-104F, 20F, 22F, 208F, 1F, 0.8F, 0, 0.5F);
        bodyModel[88] = new ModelRendererTurbo(this, 437, 227, TEX_W, TEX_H); // lining
        bodyModel[88].addBox(-88F, 20F, 20F, 176F, 12F, 1F, 0, 0.5F);
        bodyModel[89] = new ModelRendererTurbo(this, 1314, 210, TEX_W, TEX_H); // lining
        bodyModel[89].addBox(-88F, 46F, 20F, 176F, 13F, 1F, 0, 0.5F);
        bodyModel[90] = new ModelRendererTurbo(this, 114, 210, TEX_W, TEX_H);
        bodyModel[90].addBox(-88F, 32F, 21F, 6F, 14F, 1F, 0, 0.5F);
        bodyModel[91] = new ModelRendererTurbo(this, 129, 210, TEX_W, TEX_H);
        bodyModel[91].addBox(-88F, 32F, 20F, 6F, 14F, 1F, 0, 0.5F);
        bodyModel[92] = new ModelRendererTurbo(this, 144, 210, TEX_W, TEX_H);
        bodyModel[92].addBox(-62F, 32F, 21F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[93] = new ModelRendererTurbo(this, 155, 210, TEX_W, TEX_H);
        bodyModel[93].addBox(-62F, 32F, 20F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[94] = new ModelRendererTurbo(this, 166, 210, TEX_W, TEX_H);
        bodyModel[94].addBox(-50F, 32F, 21F, 12F, 14F, 1F, 0, 0.5F);
        bodyModel[95] = new ModelRendererTurbo(this, 193, 210, TEX_W, TEX_H);
        bodyModel[95].addBox(-50F, 32F, 20F, 12F, 14F, 1F, 0, 0.5F);
        bodyModel[96] = new ModelRendererTurbo(this, 220, 210, TEX_W, TEX_H);
        bodyModel[96].addBox(-18F, 32F, 21F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[97] = new ModelRendererTurbo(this, 231, 210, TEX_W, TEX_H);
        bodyModel[97].addBox(-18F, 32F, 20F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[98] = new ModelRendererTurbo(this, 242, 210, TEX_W, TEX_H);
        bodyModel[98].addBox(-6F, 32F, 21F, 12F, 14F, 1F, 0, 0.5F);
        bodyModel[99] = new ModelRendererTurbo(this, 269, 210, TEX_W, TEX_H);
        bodyModel[99].addBox(-6F, 32F, 20F, 12F, 14F, 1F, 0, 0.5F);
        bodyModel[100] = new ModelRendererTurbo(this, 296, 210, TEX_W, TEX_H);
        bodyModel[100].addBox(26F, 32F, 21F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[101] = new ModelRendererTurbo(this, 307, 210, TEX_W, TEX_H);
        bodyModel[101].addBox(26F, 32F, 20F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[102] = new ModelRendererTurbo(this, 318, 210, TEX_W, TEX_H);
        bodyModel[102].addBox(38F, 32F, 21F, 12F, 14F, 1F, 0, 0.5F);
        bodyModel[103] = new ModelRendererTurbo(this, 345, 210, TEX_W, TEX_H);
        bodyModel[103].addBox(38F, 32F, 20F, 12F, 14F, 1F, 0, 0.5F);
        bodyModel[104] = new ModelRendererTurbo(this, 372, 210, TEX_W, TEX_H);
        bodyModel[104].addBox(70F, 32F, 21F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[105] = new ModelRendererTurbo(this, 383, 210, TEX_W, TEX_H);
        bodyModel[105].addBox(70F, 32F, 20F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[106] = new ModelRendererTurbo(this, 394, 210, TEX_W, TEX_H);
        bodyModel[106].addBox(82F, 32F, 21F, 6F, 14F, 1F, 0, 0.5F);
        bodyModel[107] = new ModelRendererTurbo(this, 409, 210, TEX_W, TEX_H);
        bodyModel[107].addBox(82F, 32F, 20F, 6F, 14F, 1F, 0, 0.5F);
        bodyModel[108] = new ModelRendererTurbo(this, 838, 276, TEX_W, TEX_H); // window frame
        bodyModel[108].addBox(-82.5F, 31.5F, 22F, 21F, 1F, 0.5F, 0, 0.5F);
        bodyModel[109] = new ModelRendererTurbo(this, 883, 276, TEX_W, TEX_H); // window frame
        bodyModel[109].addBox(-82.5F, 45.5F, 22F, 21F, 1F, 0.5F, 0, 0.5F);
        bodyModel[110] = new ModelRendererTurbo(this, 424, 210, TEX_W, TEX_H); // window frame
        bodyModel[110].addBox(-82.5F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[111] = new ModelRendererTurbo(this, 429, 210, TEX_W, TEX_H); // window frame
        bodyModel[111].addBox(-62F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[112] = new ModelRendererTurbo(this, 928, 276, TEX_W, TEX_H); // sill
        bodyModel[112].addBox(-82F, 31F, 20F, 20F, 1F, 1F, 0, 0.5F);
        bodyModel[113] = new ModelRendererTurbo(this, 971, 276, TEX_W, TEX_H); // sill
        bodyModel[113].addBox(-82F, 31F, 18.5F, 20F, 1F, 1.5F, 0, 0.5F);
        bodyModel[114] = new ModelRendererTurbo(this, 579, 271, TEX_W, TEX_H); // blind box
        bodyModel[114].addBox(-82F, 45F, 19F, 20F, 2F, 1F, 0, 0.5F);
        bodyModel[115] = new ModelRendererTurbo(this, 1014, 276, TEX_W, TEX_H); // window frame
        bodyModel[115].addBox(-58.5F, 31.5F, 22F, 9F, 1F, 0.5F, 0, 0.5F);
        bodyModel[116] = new ModelRendererTurbo(this, 1035, 276, TEX_W, TEX_H); // window frame
        bodyModel[116].addBox(-58.5F, 45.5F, 22F, 9F, 1F, 0.5F, 0, 0.5F);
        bodyModel[117] = new ModelRendererTurbo(this, 434, 210, TEX_W, TEX_H); // window frame
        bodyModel[117].addBox(-58.5F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[118] = new ModelRendererTurbo(this, 439, 210, TEX_W, TEX_H); // window frame
        bodyModel[118].addBox(-50F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[119] = new ModelRendererTurbo(this, 1056, 276, TEX_W, TEX_H); // sill
        bodyModel[119].addBox(-58F, 31F, 20F, 8F, 1F, 1F, 0, 0.5F);
        bodyModel[120] = new ModelRendererTurbo(this, 1075, 276, TEX_W, TEX_H); // sill
        bodyModel[120].addBox(-58F, 31F, 18.5F, 8F, 1F, 1.5F, 0, 0.5F);
        bodyModel[121] = new ModelRendererTurbo(this, 622, 271, TEX_W, TEX_H); // blind box
        bodyModel[121].addBox(-58F, 45F, 19F, 8F, 2F, 1F, 0, 0.5F);
        bodyModel[122] = new ModelRendererTurbo(this, 1094, 276, TEX_W, TEX_H); // window frame
        bodyModel[122].addBox(-38.5F, 31.5F, 22F, 21F, 1F, 0.5F, 0, 0.5F);
        bodyModel[123] = new ModelRendererTurbo(this, 1139, 276, TEX_W, TEX_H); // window frame
        bodyModel[123].addBox(-38.5F, 45.5F, 22F, 21F, 1F, 0.5F, 0, 0.5F);
        bodyModel[124] = new ModelRendererTurbo(this, 444, 210, TEX_W, TEX_H); // window frame
        bodyModel[124].addBox(-38.5F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[125] = new ModelRendererTurbo(this, 449, 210, TEX_W, TEX_H); // window frame
        bodyModel[125].addBox(-18F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[126] = new ModelRendererTurbo(this, 1184, 276, TEX_W, TEX_H); // sill
        bodyModel[126].addBox(-38F, 31F, 20F, 20F, 1F, 1F, 0, 0.5F);
        bodyModel[127] = new ModelRendererTurbo(this, 1227, 276, TEX_W, TEX_H); // sill
        bodyModel[127].addBox(-38F, 31F, 18.5F, 20F, 1F, 1.5F, 0, 0.5F);
        bodyModel[128] = new ModelRendererTurbo(this, 641, 271, TEX_W, TEX_H); // blind box
        bodyModel[128].addBox(-38F, 45F, 19F, 20F, 2F, 1F, 0, 0.5F);
        bodyModel[129] = new ModelRendererTurbo(this, 1270, 276, TEX_W, TEX_H); // window frame
        bodyModel[129].addBox(-14.5F, 31.5F, 22F, 9F, 1F, 0.5F, 0, 0.5F);
        bodyModel[130] = new ModelRendererTurbo(this, 1291, 276, TEX_W, TEX_H); // window frame
        bodyModel[130].addBox(-14.5F, 45.5F, 22F, 9F, 1F, 0.5F, 0, 0.5F);
        bodyModel[131] = new ModelRendererTurbo(this, 454, 210, TEX_W, TEX_H); // window frame
        bodyModel[131].addBox(-14.5F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[132] = new ModelRendererTurbo(this, 459, 210, TEX_W, TEX_H); // window frame
        bodyModel[132].addBox(-6F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[133] = new ModelRendererTurbo(this, 1312, 276, TEX_W, TEX_H); // sill
        bodyModel[133].addBox(-14F, 31F, 20F, 8F, 1F, 1F, 0, 0.5F);
        bodyModel[134] = new ModelRendererTurbo(this, 1331, 276, TEX_W, TEX_H); // sill
        bodyModel[134].addBox(-14F, 31F, 18.5F, 8F, 1F, 1.5F, 0, 0.5F);
        bodyModel[135] = new ModelRendererTurbo(this, 684, 271, TEX_W, TEX_H); // blind box
        bodyModel[135].addBox(-14F, 45F, 19F, 8F, 2F, 1F, 0, 0.5F);
        bodyModel[136] = new ModelRendererTurbo(this, 1350, 276, TEX_W, TEX_H); // window frame
        bodyModel[136].addBox(5.5F, 31.5F, 22F, 21F, 1F, 0.5F, 0, 0.5F);
        bodyModel[137] = new ModelRendererTurbo(this, 1395, 276, TEX_W, TEX_H); // window frame
        bodyModel[137].addBox(5.5F, 45.5F, 22F, 21F, 1F, 0.5F, 0, 0.5F);
        bodyModel[138] = new ModelRendererTurbo(this, 464, 210, TEX_W, TEX_H); // window frame
        bodyModel[138].addBox(5.5F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[139] = new ModelRendererTurbo(this, 469, 210, TEX_W, TEX_H); // window frame
        bodyModel[139].addBox(26F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[140] = new ModelRendererTurbo(this, 1440, 276, TEX_W, TEX_H); // sill
        bodyModel[140].addBox(6F, 31F, 20F, 20F, 1F, 1F, 0, 0.5F);
        bodyModel[141] = new ModelRendererTurbo(this, 1483, 276, TEX_W, TEX_H); // sill
        bodyModel[141].addBox(6F, 31F, 18.5F, 20F, 1F, 1.5F, 0, 0.5F);
        bodyModel[142] = new ModelRendererTurbo(this, 703, 271, TEX_W, TEX_H); // blind box
        bodyModel[142].addBox(6F, 45F, 19F, 20F, 2F, 1F, 0, 0.5F);
        bodyModel[143] = new ModelRendererTurbo(this, 1526, 276, TEX_W, TEX_H); // window frame
        bodyModel[143].addBox(29.5F, 31.5F, 22F, 9F, 1F, 0.5F, 0, 0.5F);
        bodyModel[144] = new ModelRendererTurbo(this, 1547, 276, TEX_W, TEX_H); // window frame
        bodyModel[144].addBox(29.5F, 45.5F, 22F, 9F, 1F, 0.5F, 0, 0.5F);
        bodyModel[145] = new ModelRendererTurbo(this, 474, 210, TEX_W, TEX_H); // window frame
        bodyModel[145].addBox(29.5F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[146] = new ModelRendererTurbo(this, 479, 210, TEX_W, TEX_H); // window frame
        bodyModel[146].addBox(38F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[147] = new ModelRendererTurbo(this, 1568, 276, TEX_W, TEX_H); // sill
        bodyModel[147].addBox(30F, 31F, 20F, 8F, 1F, 1F, 0, 0.5F);
        bodyModel[148] = new ModelRendererTurbo(this, 1587, 276, TEX_W, TEX_H); // sill
        bodyModel[148].addBox(30F, 31F, 18.5F, 8F, 1F, 1.5F, 0, 0.5F);
        bodyModel[149] = new ModelRendererTurbo(this, 746, 271, TEX_W, TEX_H); // blind box
        bodyModel[149].addBox(30F, 45F, 19F, 8F, 2F, 1F, 0, 0.5F);
        bodyModel[150] = new ModelRendererTurbo(this, 1606, 276, TEX_W, TEX_H); // window frame
        bodyModel[150].addBox(49.5F, 31.5F, 22F, 21F, 1F, 0.5F, 0, 0.5F);
        bodyModel[151] = new ModelRendererTurbo(this, 1651, 276, TEX_W, TEX_H); // window frame
        bodyModel[151].addBox(49.5F, 45.5F, 22F, 21F, 1F, 0.5F, 0, 0.5F);
        bodyModel[152] = new ModelRendererTurbo(this, 484, 210, TEX_W, TEX_H); // window frame
        bodyModel[152].addBox(49.5F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[153] = new ModelRendererTurbo(this, 489, 210, TEX_W, TEX_H); // window frame
        bodyModel[153].addBox(70F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[154] = new ModelRendererTurbo(this, 1696, 276, TEX_W, TEX_H); // sill
        bodyModel[154].addBox(50F, 31F, 20F, 20F, 1F, 1F, 0, 0.5F);
        bodyModel[155] = new ModelRendererTurbo(this, 1739, 276, TEX_W, TEX_H); // sill
        bodyModel[155].addBox(50F, 31F, 18.5F, 20F, 1F, 1.5F, 0, 0.5F);
        bodyModel[156] = new ModelRendererTurbo(this, 765, 271, TEX_W, TEX_H); // blind box
        bodyModel[156].addBox(50F, 45F, 19F, 20F, 2F, 1F, 0, 0.5F);
        bodyModel[157] = new ModelRendererTurbo(this, 1782, 276, TEX_W, TEX_H); // window frame
        bodyModel[157].addBox(73.5F, 31.5F, 22F, 9F, 1F, 0.5F, 0, 0.5F);
        bodyModel[158] = new ModelRendererTurbo(this, 1803, 276, TEX_W, TEX_H); // window frame
        bodyModel[158].addBox(73.5F, 45.5F, 22F, 9F, 1F, 0.5F, 0, 0.5F);
        bodyModel[159] = new ModelRendererTurbo(this, 494, 210, TEX_W, TEX_H); // window frame
        bodyModel[159].addBox(73.5F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[160] = new ModelRendererTurbo(this, 499, 210, TEX_W, TEX_H); // window frame
        bodyModel[160].addBox(82F, 32F, 22F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[161] = new ModelRendererTurbo(this, 1824, 276, TEX_W, TEX_H); // sill
        bodyModel[161].addBox(74F, 31F, 20F, 8F, 1F, 1F, 0, 0.5F);
        bodyModel[162] = new ModelRendererTurbo(this, 1843, 276, TEX_W, TEX_H); // sill
        bodyModel[162].addBox(74F, 31F, 18.5F, 8F, 1F, 1.5F, 0, 0.5F);
        bodyModel[163] = new ModelRendererTurbo(this, 808, 271, TEX_W, TEX_H); // blind box
        bodyModel[163].addBox(74F, 45F, 19F, 8F, 2F, 1F, 0, 0.5F);
        bodyModel[164] = new ModelRendererTurbo(this, 104, 255, TEX_W, TEX_H); // crest
        bodyModel[164].addBox(-5F, 47F, 22F, 10F, 6F, 0.4F, 0, 0.5F);
        bodyModel[165] = new ModelRendererTurbo(this, 504, 210, TEX_W, TEX_H);
        bodyModel[165].addBox(-90F, 32F, 21F, 2F, 14F, 1F, 0, 0.5F);
        bodyModel[166] = new ModelRendererTurbo(this, 511, 210, TEX_W, TEX_H);
        bodyModel[166].addBox(-104F, 32F, 21F, 2F, 14F, 1F, 0, 0.5F);
        bodyModel[167] = new ModelRendererTurbo(this, 792, 227, TEX_W, TEX_H);
        bodyModel[167].addBox(-102F, 20F, 21F, 12F, 12F, 1F, 0, 0.5F);
        bodyModel[168] = new ModelRendererTurbo(this, 518, 210, TEX_W, TEX_H);
        bodyModel[168].addBox(-102F, 32F, 21F, 3F, 14F, 1F, 0, 0.5F);
        bodyModel[169] = new ModelRendererTurbo(this, 527, 210, TEX_W, TEX_H);
        bodyModel[169].addBox(-93F, 32F, 21F, 3F, 14F, 1F, 0, 0.5F);
        bodyModel[170] = new ModelRendererTurbo(this, 536, 210, TEX_W, TEX_H); // door window
        bodyModel[170].addBox(-99F, 32F, 21.2F, 6F, 14F, 0.4F, 0, 0.5F);
        bodyModel[171] = new ModelRendererTurbo(this, 1862, 276, TEX_W, TEX_H); // door window frame
        bodyModel[171].addBox(-99.5F, 31.5F, 22F, 7F, 1F, 0.5F, 0, 0.5F);
        bodyModel[172] = new ModelRendererTurbo(this, 1879, 276, TEX_W, TEX_H); // door window frame
        bodyModel[172].addBox(-99.5F, 45.5F, 22F, 7F, 1F, 0.5F, 0, 0.5F);
        bodyModel[173] = new ModelRendererTurbo(this, 1896, 276, TEX_W, TEX_H); // door handle
        bodyModel[173].addBox(-94F, 26F, 22F, 2F, 1F, 1.2F, 0, 0.5F);
        bodyModel[174] = new ModelRendererTurbo(this, 551, 210, TEX_W, TEX_H);
        bodyModel[174].addBox(88F, 32F, 21F, 2F, 14F, 1F, 0, 0.5F);
        bodyModel[175] = new ModelRendererTurbo(this, 558, 210, TEX_W, TEX_H);
        bodyModel[175].addBox(102F, 32F, 21F, 2F, 14F, 1F, 0, 0.5F);
        bodyModel[176] = new ModelRendererTurbo(this, 819, 227, TEX_W, TEX_H);
        bodyModel[176].addBox(90F, 20F, 21F, 12F, 12F, 1F, 0, 0.5F);
        bodyModel[177] = new ModelRendererTurbo(this, 565, 210, TEX_W, TEX_H);
        bodyModel[177].addBox(90F, 32F, 21F, 3F, 14F, 1F, 0, 0.5F);
        bodyModel[178] = new ModelRendererTurbo(this, 574, 210, TEX_W, TEX_H);
        bodyModel[178].addBox(99F, 32F, 21F, 3F, 14F, 1F, 0, 0.5F);
        bodyModel[179] = new ModelRendererTurbo(this, 583, 210, TEX_W, TEX_H); // door window
        bodyModel[179].addBox(93F, 32F, 21.2F, 6F, 14F, 0.4F, 0, 0.5F);
        bodyModel[180] = new ModelRendererTurbo(this, 1903, 276, TEX_W, TEX_H); // door window frame
        bodyModel[180].addBox(92.5F, 31.5F, 22F, 7F, 1F, 0.5F, 0, 0.5F);
        bodyModel[181] = new ModelRendererTurbo(this, 1920, 276, TEX_W, TEX_H); // door window frame
        bodyModel[181].addBox(92.5F, 45.5F, 22F, 7F, 1F, 0.5F, 0, 0.5F);
        bodyModel[182] = new ModelRendererTurbo(this, 1937, 276, TEX_W, TEX_H); // door handle
        bodyModel[182].addBox(98F, 26F, 22F, 2F, 1F, 1.2F, 0, 0.5F);
        bodyModel[183] = new ModelRendererTurbo(this, 846, 227, TEX_W, TEX_H);
        bodyModel[183].addBox(-104F, 20F, -22F, 208F, 12F, 1F, 0, 0.5F);
        bodyModel[184] = new ModelRendererTurbo(this, 419, 242, TEX_W, TEX_H);
        bodyModel[184].addBox(-104F, 46F, -22F, 208F, 11F, 1F, 0, 0.5F);
        bodyModel[185] = new ModelRendererTurbo(this, 827, 271, TEX_W, TEX_H);
        bodyModel[185].addBox(-104F, 57F, -22F, 208F, 2F, 1F, 0, 0.5F);
        bodyModel[186] = new ModelRendererTurbo(this, 0, 279, TEX_W, TEX_H); // waist rail
        bodyModel[186].addBox(-104F, 30.5F, -22.6F, 208F, 1.5F, 0.6F, 0, 0.5F);
        bodyModel[187] = new ModelRendererTurbo(this, 419, 279, TEX_W, TEX_H); // cant rail
        bodyModel[187].addBox(-104F, 56.5F, -22.6F, 208F, 1F, 0.6F, 0, 0.5F);
        bodyModel[188] = new ModelRendererTurbo(this, 838, 279, TEX_W, TEX_H); // bottom rail
        bodyModel[188].addBox(-104F, 20F, -22.8F, 208F, 1F, 0.8F, 0, 0.5F);
        bodyModel[189] = new ModelRendererTurbo(this, 1265, 227, TEX_W, TEX_H); // lining
        bodyModel[189].addBox(-88F, 20F, -21F, 176F, 12F, 1F, 0, 0.5F);
        bodyModel[190] = new ModelRendererTurbo(this, 1669, 210, TEX_W, TEX_H); // lining
        bodyModel[190].addBox(-88F, 46F, -21F, 176F, 13F, 1F, 0, 0.5F);
        bodyModel[191] = new ModelRendererTurbo(this, 598, 210, TEX_W, TEX_H);
        bodyModel[191].addBox(-88F, 32F, -22F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[192] = new ModelRendererTurbo(this, 609, 210, TEX_W, TEX_H);
        bodyModel[192].addBox(-88F, 32F, -21F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[193] = new ModelRendererTurbo(this, 620, 210, TEX_W, TEX_H);
        bodyModel[193].addBox(-70F, 32F, -22F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[194] = new ModelRendererTurbo(this, 639, 210, TEX_W, TEX_H);
        bodyModel[194].addBox(-70F, 32F, -21F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[195] = new ModelRendererTurbo(this, 658, 210, TEX_W, TEX_H);
        bodyModel[195].addBox(-48F, 32F, -22F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[196] = new ModelRendererTurbo(this, 677, 210, TEX_W, TEX_H);
        bodyModel[196].addBox(-48F, 32F, -21F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[197] = new ModelRendererTurbo(this, 696, 210, TEX_W, TEX_H);
        bodyModel[197].addBox(-26F, 32F, -22F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[198] = new ModelRendererTurbo(this, 715, 210, TEX_W, TEX_H);
        bodyModel[198].addBox(-26F, 32F, -21F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[199] = new ModelRendererTurbo(this, 734, 210, TEX_W, TEX_H);
        bodyModel[199].addBox(-4F, 32F, -22F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[200] = new ModelRendererTurbo(this, 753, 210, TEX_W, TEX_H);
        bodyModel[200].addBox(-4F, 32F, -21F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[201] = new ModelRendererTurbo(this, 772, 210, TEX_W, TEX_H);
        bodyModel[201].addBox(18F, 32F, -22F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[202] = new ModelRendererTurbo(this, 791, 210, TEX_W, TEX_H);
        bodyModel[202].addBox(18F, 32F, -21F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[203] = new ModelRendererTurbo(this, 810, 210, TEX_W, TEX_H);
        bodyModel[203].addBox(40F, 32F, -22F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[204] = new ModelRendererTurbo(this, 829, 210, TEX_W, TEX_H);
        bodyModel[204].addBox(40F, 32F, -21F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[205] = new ModelRendererTurbo(this, 848, 210, TEX_W, TEX_H);
        bodyModel[205].addBox(62F, 32F, -22F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[206] = new ModelRendererTurbo(this, 867, 210, TEX_W, TEX_H);
        bodyModel[206].addBox(62F, 32F, -21F, 8F, 14F, 1F, 0, 0.5F);
        bodyModel[207] = new ModelRendererTurbo(this, 886, 210, TEX_W, TEX_H);
        bodyModel[207].addBox(84F, 32F, -22F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[208] = new ModelRendererTurbo(this, 897, 210, TEX_W, TEX_H);
        bodyModel[208].addBox(84F, 32F, -21F, 4F, 14F, 1F, 0, 0.5F);
        bodyModel[209] = new ModelRendererTurbo(this, 1257, 279, TEX_W, TEX_H); // window frame
        bodyModel[209].addBox(-84.5F, 31.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[210] = new ModelRendererTurbo(this, 1290, 279, TEX_W, TEX_H); // window frame
        bodyModel[210].addBox(-84.5F, 45.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[211] = new ModelRendererTurbo(this, 908, 210, TEX_W, TEX_H); // window frame
        bodyModel[211].addBox(-84.5F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[212] = new ModelRendererTurbo(this, 913, 210, TEX_W, TEX_H); // window frame
        bodyModel[212].addBox(-70F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[213] = new ModelRendererTurbo(this, 1323, 279, TEX_W, TEX_H); // sill
        bodyModel[213].addBox(-84F, 31F, -21F, 14F, 1F, 1F, 0, 0.5F);
        bodyModel[214] = new ModelRendererTurbo(this, 1354, 279, TEX_W, TEX_H); // sill
        bodyModel[214].addBox(-84F, 31F, -20F, 14F, 1F, 1.5F, 0, 0.5F);
        bodyModel[215] = new ModelRendererTurbo(this, 1246, 271, TEX_W, TEX_H); // blind box
        bodyModel[215].addBox(-84F, 45F, -20F, 14F, 2F, 1F, 0, 0.5F);
        bodyModel[216] = new ModelRendererTurbo(this, 1385, 279, TEX_W, TEX_H); // window frame
        bodyModel[216].addBox(-62.5F, 31.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[217] = new ModelRendererTurbo(this, 1418, 279, TEX_W, TEX_H); // window frame
        bodyModel[217].addBox(-62.5F, 45.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[218] = new ModelRendererTurbo(this, 918, 210, TEX_W, TEX_H); // window frame
        bodyModel[218].addBox(-62.5F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[219] = new ModelRendererTurbo(this, 923, 210, TEX_W, TEX_H); // window frame
        bodyModel[219].addBox(-48F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[220] = new ModelRendererTurbo(this, 1451, 279, TEX_W, TEX_H); // sill
        bodyModel[220].addBox(-62F, 31F, -21F, 14F, 1F, 1F, 0, 0.5F);
        bodyModel[221] = new ModelRendererTurbo(this, 1482, 279, TEX_W, TEX_H); // sill
        bodyModel[221].addBox(-62F, 31F, -20F, 14F, 1F, 1.5F, 0, 0.5F);
        bodyModel[222] = new ModelRendererTurbo(this, 1277, 271, TEX_W, TEX_H); // blind box
        bodyModel[222].addBox(-62F, 45F, -20F, 14F, 2F, 1F, 0, 0.5F);
        bodyModel[223] = new ModelRendererTurbo(this, 1513, 279, TEX_W, TEX_H); // window frame
        bodyModel[223].addBox(-40.5F, 31.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[224] = new ModelRendererTurbo(this, 1546, 279, TEX_W, TEX_H); // window frame
        bodyModel[224].addBox(-40.5F, 45.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[225] = new ModelRendererTurbo(this, 928, 210, TEX_W, TEX_H); // window frame
        bodyModel[225].addBox(-40.5F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[226] = new ModelRendererTurbo(this, 933, 210, TEX_W, TEX_H); // window frame
        bodyModel[226].addBox(-26F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[227] = new ModelRendererTurbo(this, 1579, 279, TEX_W, TEX_H); // sill
        bodyModel[227].addBox(-40F, 31F, -21F, 14F, 1F, 1F, 0, 0.5F);
        bodyModel[228] = new ModelRendererTurbo(this, 1610, 279, TEX_W, TEX_H); // sill
        bodyModel[228].addBox(-40F, 31F, -20F, 14F, 1F, 1.5F, 0, 0.5F);
        bodyModel[229] = new ModelRendererTurbo(this, 1308, 271, TEX_W, TEX_H); // blind box
        bodyModel[229].addBox(-40F, 45F, -20F, 14F, 2F, 1F, 0, 0.5F);
        bodyModel[230] = new ModelRendererTurbo(this, 1641, 279, TEX_W, TEX_H); // window frame
        bodyModel[230].addBox(-18.5F, 31.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[231] = new ModelRendererTurbo(this, 1674, 279, TEX_W, TEX_H); // window frame
        bodyModel[231].addBox(-18.5F, 45.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[232] = new ModelRendererTurbo(this, 938, 210, TEX_W, TEX_H); // window frame
        bodyModel[232].addBox(-18.5F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[233] = new ModelRendererTurbo(this, 943, 210, TEX_W, TEX_H); // window frame
        bodyModel[233].addBox(-4F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[234] = new ModelRendererTurbo(this, 1707, 279, TEX_W, TEX_H); // sill
        bodyModel[234].addBox(-18F, 31F, -21F, 14F, 1F, 1F, 0, 0.5F);
        bodyModel[235] = new ModelRendererTurbo(this, 1738, 279, TEX_W, TEX_H); // sill
        bodyModel[235].addBox(-18F, 31F, -20F, 14F, 1F, 1.5F, 0, 0.5F);
        bodyModel[236] = new ModelRendererTurbo(this, 1339, 271, TEX_W, TEX_H); // blind box
        bodyModel[236].addBox(-18F, 45F, -20F, 14F, 2F, 1F, 0, 0.5F);
        bodyModel[237] = new ModelRendererTurbo(this, 1769, 279, TEX_W, TEX_H); // window frame
        bodyModel[237].addBox(3.5F, 31.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[238] = new ModelRendererTurbo(this, 1802, 279, TEX_W, TEX_H); // window frame
        bodyModel[238].addBox(3.5F, 45.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[239] = new ModelRendererTurbo(this, 948, 210, TEX_W, TEX_H); // window frame
        bodyModel[239].addBox(3.5F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[240] = new ModelRendererTurbo(this, 953, 210, TEX_W, TEX_H); // window frame
        bodyModel[240].addBox(18F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[241] = new ModelRendererTurbo(this, 1835, 279, TEX_W, TEX_H); // sill
        bodyModel[241].addBox(4F, 31F, -21F, 14F, 1F, 1F, 0, 0.5F);
        bodyModel[242] = new ModelRendererTurbo(this, 1866, 279, TEX_W, TEX_H); // sill
        bodyModel[242].addBox(4F, 31F, -20F, 14F, 1F, 1.5F, 0, 0.5F);
        bodyModel[243] = new ModelRendererTurbo(this, 1370, 271, TEX_W, TEX_H); // blind box
        bodyModel[243].addBox(4F, 45F, -20F, 14F, 2F, 1F, 0, 0.5F);
        bodyModel[244] = new ModelRendererTurbo(this, 1897, 279, TEX_W, TEX_H); // window frame
        bodyModel[244].addBox(25.5F, 31.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[245] = new ModelRendererTurbo(this, 1930, 279, TEX_W, TEX_H); // window frame
        bodyModel[245].addBox(25.5F, 45.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[246] = new ModelRendererTurbo(this, 958, 210, TEX_W, TEX_H); // window frame
        bodyModel[246].addBox(25.5F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[247] = new ModelRendererTurbo(this, 963, 210, TEX_W, TEX_H); // window frame
        bodyModel[247].addBox(40F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[248] = new ModelRendererTurbo(this, 1963, 279, TEX_W, TEX_H); // sill
        bodyModel[248].addBox(26F, 31F, -21F, 14F, 1F, 1F, 0, 0.5F);
        bodyModel[249] = new ModelRendererTurbo(this, 1994, 279, TEX_W, TEX_H); // sill
        bodyModel[249].addBox(26F, 31F, -20F, 14F, 1F, 1.5F, 0, 0.5F);
        bodyModel[250] = new ModelRendererTurbo(this, 1401, 271, TEX_W, TEX_H); // blind box
        bodyModel[250].addBox(26F, 45F, -20F, 14F, 2F, 1F, 0, 0.5F);
        bodyModel[251] = new ModelRendererTurbo(this, 0, 282, TEX_W, TEX_H); // window frame
        bodyModel[251].addBox(47.5F, 31.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[252] = new ModelRendererTurbo(this, 33, 282, TEX_W, TEX_H); // window frame
        bodyModel[252].addBox(47.5F, 45.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[253] = new ModelRendererTurbo(this, 968, 210, TEX_W, TEX_H); // window frame
        bodyModel[253].addBox(47.5F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[254] = new ModelRendererTurbo(this, 973, 210, TEX_W, TEX_H); // window frame
        bodyModel[254].addBox(62F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[255] = new ModelRendererTurbo(this, 66, 282, TEX_W, TEX_H); // sill
        bodyModel[255].addBox(48F, 31F, -21F, 14F, 1F, 1F, 0, 0.5F);
        bodyModel[256] = new ModelRendererTurbo(this, 97, 282, TEX_W, TEX_H); // sill
        bodyModel[256].addBox(48F, 31F, -20F, 14F, 1F, 1.5F, 0, 0.5F);
        bodyModel[257] = new ModelRendererTurbo(this, 1432, 271, TEX_W, TEX_H); // blind box
        bodyModel[257].addBox(48F, 45F, -20F, 14F, 2F, 1F, 0, 0.5F);
        bodyModel[258] = new ModelRendererTurbo(this, 128, 282, TEX_W, TEX_H); // window frame
        bodyModel[258].addBox(69.5F, 31.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[259] = new ModelRendererTurbo(this, 161, 282, TEX_W, TEX_H); // window frame
        bodyModel[259].addBox(69.5F, 45.5F, -22.5F, 15F, 1F, 0.5F, 0, 0.5F);
        bodyModel[260] = new ModelRendererTurbo(this, 978, 210, TEX_W, TEX_H); // window frame
        bodyModel[260].addBox(69.5F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[261] = new ModelRendererTurbo(this, 983, 210, TEX_W, TEX_H); // window frame
        bodyModel[261].addBox(84F, 32F, -22.5F, 0.5F, 14F, 0.5F, 0, 0.5F);
        bodyModel[262] = new ModelRendererTurbo(this, 194, 282, TEX_W, TEX_H); // sill
        bodyModel[262].addBox(70F, 31F, -21F, 14F, 1F, 1F, 0, 0.5F);
        bodyModel[263] = new ModelRendererTurbo(this, 225, 282, TEX_W, TEX_H); // sill
        bodyModel[263].addBox(70F, 31F, -20F, 14F, 1F, 1.5F, 0, 0.5F);
        bodyModel[264] = new ModelRendererTurbo(this, 1463, 271, TEX_W, TEX_H); // blind box
        bodyModel[264].addBox(70F, 45F, -20F, 14F, 2F, 1F, 0, 0.5F);
        bodyModel[265] = new ModelRendererTurbo(this, 127, 255, TEX_W, TEX_H); // crest
        bodyModel[265].addBox(-5F, 47F, -22.4F, 10F, 6F, 0.4F, 0, 0.5F);
        bodyModel[266] = new ModelRendererTurbo(this, 988, 210, TEX_W, TEX_H);
        bodyModel[266].addBox(-90F, 32F, -22F, 2F, 14F, 1F, 0, 0.5F);
        bodyModel[267] = new ModelRendererTurbo(this, 995, 210, TEX_W, TEX_H);
        bodyModel[267].addBox(-104F, 32F, -22F, 2F, 14F, 1F, 0, 0.5F);
        bodyModel[268] = new ModelRendererTurbo(this, 1620, 227, TEX_W, TEX_H);
        bodyModel[268].addBox(-102F, 20F, -22F, 12F, 12F, 1F, 0, 0.5F);
        bodyModel[269] = new ModelRendererTurbo(this, 1002, 210, TEX_W, TEX_H);
        bodyModel[269].addBox(-102F, 32F, -22F, 3F, 14F, 1F, 0, 0.5F);
        bodyModel[270] = new ModelRendererTurbo(this, 1011, 210, TEX_W, TEX_H);
        bodyModel[270].addBox(-93F, 32F, -22F, 3F, 14F, 1F, 0, 0.5F);
        bodyModel[271] = new ModelRendererTurbo(this, 1020, 210, TEX_W, TEX_H); // door window
        bodyModel[271].addBox(-99F, 32F, -21.6F, 6F, 14F, 0.4F, 0, 0.5F);
        bodyModel[272] = new ModelRendererTurbo(this, 256, 282, TEX_W, TEX_H); // door window frame
        bodyModel[272].addBox(-99.5F, 31.5F, -22.5F, 7F, 1F, 0.5F, 0, 0.5F);
        bodyModel[273] = new ModelRendererTurbo(this, 273, 282, TEX_W, TEX_H); // door window frame
        bodyModel[273].addBox(-99.5F, 45.5F, -22.5F, 7F, 1F, 0.5F, 0, 0.5F);
        bodyModel[274] = new ModelRendererTurbo(this, 290, 282, TEX_W, TEX_H); // door handle
        bodyModel[274].addBox(-94F, 26F, -23.2F, 2F, 1F, 1.2F, 0, 0.5F);
        bodyModel[275] = new ModelRendererTurbo(this, 1035, 210, TEX_W, TEX_H);
        bodyModel[275].addBox(88F, 32F, -22F, 2F, 14F, 1F, 0, 0.5F);
        bodyModel[276] = new ModelRendererTurbo(this, 1042, 210, TEX_W, TEX_H);
        bodyModel[276].addBox(102F, 32F, -22F, 2F, 14F, 1F, 0, 0.5F);
        bodyModel[277] = new ModelRendererTurbo(this, 1647, 227, TEX_W, TEX_H);
        bodyModel[277].addBox(90F, 20F, -22F, 12F, 12F, 1F, 0, 0.5F);
        bodyModel[278] = new ModelRendererTurbo(this, 1049, 210, TEX_W, TEX_H);
        bodyModel[278].addBox(90F, 32F, -22F, 3F, 14F, 1F, 0, 0.5F);
        bodyModel[279] = new ModelRendererTurbo(this, 1058, 210, TEX_W, TEX_H);
        bodyModel[279].addBox(99F, 32F, -22F, 3F, 14F, 1F, 0, 0.5F);
        bodyModel[280] = new ModelRendererTurbo(this, 1067, 210, TEX_W, TEX_H); // door window
        bodyModel[280].addBox(93F, 32F, -21.6F, 6F, 14F, 0.4F, 0, 0.5F);
        bodyModel[281] = new ModelRendererTurbo(this, 297, 282, TEX_W, TEX_H); // door window frame
        bodyModel[281].addBox(92.5F, 31.5F, -22.5F, 7F, 1F, 0.5F, 0, 0.5F);
        bodyModel[282] = new ModelRendererTurbo(this, 314, 282, TEX_W, TEX_H); // door window frame
        bodyModel[282].addBox(92.5F, 45.5F, -22.5F, 7F, 1F, 0.5F, 0, 0.5F);
        bodyModel[283] = new ModelRendererTurbo(this, 331, 282, TEX_W, TEX_H); // door handle
        bodyModel[283].addBox(98F, 26F, -23.2F, 2F, 1F, 1.2F, 0, 0.5F);
        bodyModel[284] = new ModelRendererTurbo(this, 285, 0, TEX_W, TEX_H); // end wall
        bodyModel[284].addBox(-104F, 20F, -22F, 2F, 41F, 16F, 0, 0.5F);
        bodyModel[285] = new ModelRendererTurbo(this, 322, 0, TEX_W, TEX_H); // end wall
        bodyModel[285].addBox(-104F, 20F, 6F, 2F, 41F, 16F, 0, 0.5F);
        bodyModel[286] = new ModelRendererTurbo(this, 1164, 184, TEX_W, TEX_H); // over the gangway door
        bodyModel[286].addBox(-104F, 52F, -6F, 2F, 9F, 12F, 0, 0.5F);
        bodyModel[287] = new ModelRendererTurbo(this, 990, 112, TEX_W, TEX_H); // bellows
        bodyModel[287].addBox(-107F, 20F, -10F, 3F, 34F, 4F, 0, 0.5F);
        bodyModel[288] = new ModelRendererTurbo(this, 1005, 112, TEX_W, TEX_H); // bellows
        bodyModel[288].addBox(-107F, 20F, 6F, 3F, 34F, 4F, 0, 0.5F);
        bodyModel[289] = new ModelRendererTurbo(this, 1050, 184, TEX_W, TEX_H); // bellows
        bodyModel[289].addBox(-107F, 52F, -10F, 3F, 3F, 20F, 0, 0.5F);
        bodyModel[290] = new ModelRendererTurbo(this, 842, 264, TEX_W, TEX_H); // tail lamp
        bodyModel[290].addBox(-106F, 38F, -16F, 2F, 4F, 2F, 0, 0.5F);
        bodyModel[291] = new ModelRendererTurbo(this, 851, 264, TEX_W, TEX_H); // tail lamp
        bodyModel[291].addBox(-106F, 38F, 14F, 2F, 4F, 2F, 0, 0.5F);
        bodyModel[292] = new ModelRendererTurbo(this, 359, 0, TEX_W, TEX_H); // end wall
        bodyModel[292].addBox(102F, 20F, -22F, 2F, 41F, 16F, 0, 0.5F);
        bodyModel[293] = new ModelRendererTurbo(this, 396, 0, TEX_W, TEX_H); // end wall
        bodyModel[293].addBox(102F, 20F, 6F, 2F, 41F, 16F, 0, 0.5F);
        bodyModel[294] = new ModelRendererTurbo(this, 1193, 184, TEX_W, TEX_H); // over the gangway door
        bodyModel[294].addBox(102F, 52F, -6F, 2F, 9F, 12F, 0, 0.5F);
        bodyModel[295] = new ModelRendererTurbo(this, 1020, 112, TEX_W, TEX_H); // bellows
        bodyModel[295].addBox(104F, 20F, -10F, 3F, 34F, 4F, 0, 0.5F);
        bodyModel[296] = new ModelRendererTurbo(this, 1035, 112, TEX_W, TEX_H); // bellows
        bodyModel[296].addBox(104F, 20F, 6F, 3F, 34F, 4F, 0, 0.5F);
        bodyModel[297] = new ModelRendererTurbo(this, 1097, 184, TEX_W, TEX_H); // bellows
        bodyModel[297].addBox(104F, 52F, -10F, 3F, 3F, 20F, 0, 0.5F);
        bodyModel[298] = new ModelRendererTurbo(this, 860, 264, TEX_W, TEX_H); // tail lamp
        bodyModel[298].addBox(104F, 38F, -16F, 2F, 4F, 2F, 0, 0.5F);
        bodyModel[299] = new ModelRendererTurbo(this, 869, 264, TEX_W, TEX_H); // tail lamp
        bodyModel[299].addBox(104F, 38F, 14F, 2F, 4F, 2F, 0, 0.5F);
        bodyModel[300] = new ModelRendererTurbo(this, 1172, 66, TEX_W, TEX_H); // corridor wall
        bodyModel[300].addBox(-88F, 20F, -8F, 30F, 39F, 2F, 0, 0.5F);
        bodyModel[301] = new ModelRendererTurbo(this, 1237, 66, TEX_W, TEX_H); // corridor wall
        bodyModel[301].addBox(-48F, 20F, -8F, 4F, 39F, 2F, 0, 0.5F);
        bodyModel[302] = new ModelRendererTurbo(this, 970, 242, TEX_W, TEX_H); // over the door
        bodyModel[302].addBox(-58F, 50F, -8F, 10F, 9F, 2F, 0, 0.5F);
        bodyModel[303] = new ModelRendererTurbo(this, 1050, 112, TEX_W, TEX_H); // door frame
        bodyModel[303].addBox(-58F, 20F, -9F, 1F, 30F, 4F, 0, 0.5F);
        bodyModel[304] = new ModelRendererTurbo(this, 1061, 112, TEX_W, TEX_H); // door frame
        bodyModel[304].addBox(-49F, 20F, -9F, 1F, 30F, 4F, 0, 0.5F);
        bodyModel[305] = new ModelRendererTurbo(this, 1182, 264, TEX_W, TEX_H); // door frame
        bodyModel[305].addBox(-58F, 49F, -9F, 10F, 1F, 4F, 0, 0.5F);
        bodyModel[306] = new ModelRendererTurbo(this, 1494, 271, TEX_W, TEX_H); // number plate
        bodyModel[306].addBox(-55F, 51F, -8.6F, 4F, 2F, 0.6F, 0, 0.5F);
        bodyModel[307] = new ModelRendererTurbo(this, 891, 152, TEX_W, TEX_H); // compartment door, open
        bodyModel[307].addBox(-69F, 21F, -5.8F, 10F, 28F, 0.8F, 0, 0.5F);
        bodyModel[308] = new ModelRendererTurbo(this, 1250, 66, TEX_W, TEX_H); // corridor wall
        bodyModel[308].addBox(-44F, 20F, -8F, 30F, 39F, 2F, 0, 0.5F);
        bodyModel[309] = new ModelRendererTurbo(this, 1315, 66, TEX_W, TEX_H); // corridor wall
        bodyModel[309].addBox(-4F, 20F, -8F, 4F, 39F, 2F, 0, 0.5F);
        bodyModel[310] = new ModelRendererTurbo(this, 995, 242, TEX_W, TEX_H); // over the door
        bodyModel[310].addBox(-14F, 50F, -8F, 10F, 9F, 2F, 0, 0.5F);
        bodyModel[311] = new ModelRendererTurbo(this, 1072, 112, TEX_W, TEX_H); // door frame
        bodyModel[311].addBox(-14F, 20F, -9F, 1F, 30F, 4F, 0, 0.5F);
        bodyModel[312] = new ModelRendererTurbo(this, 1083, 112, TEX_W, TEX_H); // door frame
        bodyModel[312].addBox(-5F, 20F, -9F, 1F, 30F, 4F, 0, 0.5F);
        bodyModel[313] = new ModelRendererTurbo(this, 1211, 264, TEX_W, TEX_H); // door frame
        bodyModel[313].addBox(-14F, 49F, -9F, 10F, 1F, 4F, 0, 0.5F);
        bodyModel[314] = new ModelRendererTurbo(this, 1505, 271, TEX_W, TEX_H); // number plate
        bodyModel[314].addBox(-11F, 51F, -8.6F, 4F, 2F, 0.6F, 0, 0.5F);
        bodyModel[315] = new ModelRendererTurbo(this, 914, 152, TEX_W, TEX_H); // compartment door, open
        bodyModel[315].addBox(-25F, 21F, -5.8F, 10F, 28F, 0.8F, 0, 0.5F);
        bodyModel[316] = new ModelRendererTurbo(this, 1328, 66, TEX_W, TEX_H); // corridor wall
        bodyModel[316].addBox(0F, 20F, -8F, 30F, 39F, 2F, 0, 0.5F);
        bodyModel[317] = new ModelRendererTurbo(this, 1393, 66, TEX_W, TEX_H); // corridor wall
        bodyModel[317].addBox(40F, 20F, -8F, 4F, 39F, 2F, 0, 0.5F);
        bodyModel[318] = new ModelRendererTurbo(this, 1020, 242, TEX_W, TEX_H); // over the door
        bodyModel[318].addBox(30F, 50F, -8F, 10F, 9F, 2F, 0, 0.5F);
        bodyModel[319] = new ModelRendererTurbo(this, 1094, 112, TEX_W, TEX_H); // door frame
        bodyModel[319].addBox(30F, 20F, -9F, 1F, 30F, 4F, 0, 0.5F);
        bodyModel[320] = new ModelRendererTurbo(this, 1105, 112, TEX_W, TEX_H); // door frame
        bodyModel[320].addBox(39F, 20F, -9F, 1F, 30F, 4F, 0, 0.5F);
        bodyModel[321] = new ModelRendererTurbo(this, 1240, 264, TEX_W, TEX_H); // door frame
        bodyModel[321].addBox(30F, 49F, -9F, 10F, 1F, 4F, 0, 0.5F);
        bodyModel[322] = new ModelRendererTurbo(this, 1516, 271, TEX_W, TEX_H); // number plate
        bodyModel[322].addBox(33F, 51F, -8.6F, 4F, 2F, 0.6F, 0, 0.5F);
        bodyModel[323] = new ModelRendererTurbo(this, 937, 152, TEX_W, TEX_H); // compartment door, open
        bodyModel[323].addBox(19F, 21F, -5.8F, 10F, 28F, 0.8F, 0, 0.5F);
        bodyModel[324] = new ModelRendererTurbo(this, 1406, 66, TEX_W, TEX_H); // corridor wall
        bodyModel[324].addBox(44F, 20F, -8F, 30F, 39F, 2F, 0, 0.5F);
        bodyModel[325] = new ModelRendererTurbo(this, 1471, 66, TEX_W, TEX_H); // corridor wall
        bodyModel[325].addBox(84F, 20F, -8F, 4F, 39F, 2F, 0, 0.5F);
        bodyModel[326] = new ModelRendererTurbo(this, 1045, 242, TEX_W, TEX_H); // over the door
        bodyModel[326].addBox(74F, 50F, -8F, 10F, 9F, 2F, 0, 0.5F);
        bodyModel[327] = new ModelRendererTurbo(this, 1116, 112, TEX_W, TEX_H); // door frame
        bodyModel[327].addBox(74F, 20F, -9F, 1F, 30F, 4F, 0, 0.5F);
        bodyModel[328] = new ModelRendererTurbo(this, 1127, 112, TEX_W, TEX_H); // door frame
        bodyModel[328].addBox(83F, 20F, -9F, 1F, 30F, 4F, 0, 0.5F);
        bodyModel[329] = new ModelRendererTurbo(this, 1269, 264, TEX_W, TEX_H); // door frame
        bodyModel[329].addBox(74F, 49F, -9F, 10F, 1F, 4F, 0, 0.5F);
        bodyModel[330] = new ModelRendererTurbo(this, 1527, 271, TEX_W, TEX_H); // number plate
        bodyModel[330].addBox(77F, 51F, -8.6F, 4F, 2F, 0.6F, 0, 0.5F);
        bodyModel[331] = new ModelRendererTurbo(this, 960, 152, TEX_W, TEX_H); // compartment door, open
        bodyModel[331].addBox(63F, 21F, -5.8F, 10F, 28F, 0.8F, 0, 0.5F);
        bodyModel[332] = new ModelRendererTurbo(this, 0, 0, TEX_W, TEX_H); // partition
        bodyModel[332].addBox(-45F, 20F, -6F, 2F, 39F, 26F, 0, 0.5F);
        bodyModel[333] = new ModelRendererTurbo(this, 57, 0, TEX_W, TEX_H); // partition
        bodyModel[333].addBox(-1F, 20F, -6F, 2F, 39F, 26F, 0, 0.5F);
        bodyModel[334] = new ModelRendererTurbo(this, 114, 0, TEX_W, TEX_H); // partition
        bodyModel[334].addBox(43F, 20F, -6F, 2F, 39F, 26F, 0, 0.5F);
        bodyModel[335] = new ModelRendererTurbo(this, 171, 0, TEX_W, TEX_H); // end partition
        bodyModel[335].addBox(-89F, 20F, -6F, 2F, 39F, 26F, 0, 0.5F);
        bodyModel[336] = new ModelRendererTurbo(this, 1222, 184, TEX_W, TEX_H); // over the corridor end
        bodyModel[336].addBox(-89F, 50F, -20F, 2F, 9F, 12F, 0, 0.5F);
        bodyModel[337] = new ModelRendererTurbo(this, 1484, 66, TEX_W, TEX_H); // corner
        bodyModel[337].addBox(-89F, 20F, -8F, 2F, 39F, 2F, 0, 0.5F);
        bodyModel[338] = new ModelRendererTurbo(this, 228, 0, TEX_W, TEX_H); // end partition
        bodyModel[338].addBox(87F, 20F, -6F, 2F, 39F, 26F, 0, 0.5F);
        bodyModel[339] = new ModelRendererTurbo(this, 1251, 184, TEX_W, TEX_H); // over the corridor end
        bodyModel[339].addBox(87F, 50F, -20F, 2F, 9F, 12F, 0, 0.5F);
        bodyModel[340] = new ModelRendererTurbo(this, 1493, 66, TEX_W, TEX_H); // corner
        bodyModel[340].addBox(87F, 20F, -8F, 2F, 39F, 2F, 0, 0.5F);
        bodyModel[341] = new ModelRendererTurbo(this, 1496, 112, TEX_W, TEX_H); // sofa seat
        bodyModel[341].addBox(-87F, 22F, -5F, 14F, 6F, 25F, 0, 0.5F);
        bodyModel[342] = new ModelRendererTurbo(this, 983, 152, TEX_W, TEX_H); // sofa base
        bodyModel[342].addBox(-87F, 20F, -5F, 14F, 2F, 25F, 0, 0.5F);
        bodyModel[343] = new ModelRendererTurbo(this, 1284, 112, TEX_W, TEX_H); // sofa front
        bodyModel[343].addBox(-73.5F, 20F, -5F, 1F, 7F, 25F, 0, 0.5F);
        bodyModel[344] = new ModelRendererTurbo(this, 1502, 66, TEX_W, TEX_H); // sofa back
        bodyModel[344].addBox(-87F, 28F, -5F, 4F, 16F, 25F, 0, 0.5F);
        bodyModel[345] = new ModelRendererTurbo(this, 1299, 152, TEX_W, TEX_H); // sofa back top
        bodyModel[345].addBox(-87F, 44F, -5F, 5F, 1F, 25F, 0, 0.5F);
        bodyModel[346] = new ModelRendererTurbo(this, 878, 264, TEX_W, TEX_H); // armrest
        bodyModel[346].addBox(-87F, 28F, -6F, 7F, 5F, 1F, 0, 0.5F);
        bodyModel[347] = new ModelRendererTurbo(this, 1575, 112, TEX_W, TEX_H); // upper berth, folded
        bodyModel[347].addBox(-87F, 48F, -5F, 4F, 6F, 25F, 0, 0.5F);
        bodyModel[348] = new ModelRendererTurbo(this, 1634, 112, TEX_W, TEX_H); // berth edge
        bodyModel[348].addBox(-83F, 48F, -5F, 1F, 6F, 25F, 0, 0.5F);
        bodyModel[349] = new ModelRendererTurbo(this, 425, 184, TEX_W, TEX_H); // berth rail
        bodyModel[349].addBox(-86F, 54F, -4F, 4F, 1F, 23F, 0, 0.5F);
        bodyModel[350] = new ModelRendererTurbo(this, 1122, 242, TEX_W, TEX_H); // pillow
        bodyModel[350].addBox(-84F, 38F, 6F, 3F, 4F, 6F, 0, 0.5F);
        bodyModel[351] = new ModelRendererTurbo(this, 1706, 242, TEX_W, TEX_H); // table
        bodyModel[351].addBox(-60F, 32F, 14F, 14F, 2F, 6F, 0, 0.5F);
        bodyModel[352] = new ModelRendererTurbo(this, 2024, 210, TEX_W, TEX_H); // table leg
        bodyModel[352].addBox(-54F, 20F, 16F, 2F, 12F, 2F, 0, 0.5F);
        bodyModel[353] = new ModelRendererTurbo(this, 895, 264, TEX_W, TEX_H); // table lamp
        bodyModel[353].addBox(-56F, 34F, 16F, 2F, 4F, 2F, 0, 0.5F);
        bodyModel[354] = new ModelRendererTurbo(this, 1360, 152, TEX_W, TEX_H); // washstand
        bodyModel[354].addBox(-50F, 20F, 2F, 5F, 16F, 10F, 0, 0.5F);
        bodyModel[355] = new ModelRendererTurbo(this, 838, 242, TEX_W, TEX_H); // washstand top
        bodyModel[355].addBox(-50.5F, 36F, 1.5F, 5.5F, 1F, 11F, 0, 0.5F);
        bodyModel[356] = new ModelRendererTurbo(this, 1861, 184, TEX_W, TEX_H); // mirror frame
        bodyModel[356].addBox(-45.5F, 37.5F, 2.5F, 0.5F, 11F, 9F, 0, 0.5F);
        bodyModel[357] = new ModelRendererTurbo(this, 1945, 184, TEX_W, TEX_H); // mirror
        bodyModel[357].addBox(-46F, 38F, 3F, 0.5F, 10F, 8F, 0, 0.5F);
        bodyModel[358] = new ModelRendererTurbo(this, 1747, 242, TEX_W, TEX_H); // luggage rack
        bodyModel[358].addBox(-70F, 50F, -5F, 24F, 1F, 7F, 0, 0.5F);
        bodyModel[359] = new ModelRendererTurbo(this, 1687, 112, TEX_W, TEX_H); // sofa seat
        bodyModel[359].addBox(-43F, 22F, -5F, 14F, 6F, 25F, 0, 0.5F);
        bodyModel[360] = new ModelRendererTurbo(this, 1062, 152, TEX_W, TEX_H); // sofa base
        bodyModel[360].addBox(-43F, 20F, -5F, 14F, 2F, 25F, 0, 0.5F);
        bodyModel[361] = new ModelRendererTurbo(this, 1337, 112, TEX_W, TEX_H); // sofa front
        bodyModel[361].addBox(-29.5F, 20F, -5F, 1F, 7F, 25F, 0, 0.5F);
        bodyModel[362] = new ModelRendererTurbo(this, 1561, 66, TEX_W, TEX_H); // sofa back
        bodyModel[362].addBox(-43F, 28F, -5F, 4F, 16F, 25F, 0, 0.5F);
        bodyModel[363] = new ModelRendererTurbo(this, 1391, 152, TEX_W, TEX_H); // sofa back top
        bodyModel[363].addBox(-43F, 44F, -5F, 5F, 1F, 25F, 0, 0.5F);
        bodyModel[364] = new ModelRendererTurbo(this, 904, 264, TEX_W, TEX_H); // armrest
        bodyModel[364].addBox(-43F, 28F, -6F, 7F, 5F, 1F, 0, 0.5F);
        bodyModel[365] = new ModelRendererTurbo(this, 1766, 112, TEX_W, TEX_H); // upper berth, folded
        bodyModel[365].addBox(-43F, 48F, -5F, 4F, 6F, 25F, 0, 0.5F);
        bodyModel[366] = new ModelRendererTurbo(this, 1825, 112, TEX_W, TEX_H); // berth edge
        bodyModel[366].addBox(-39F, 48F, -5F, 1F, 6F, 25F, 0, 0.5F);
        bodyModel[367] = new ModelRendererTurbo(this, 480, 184, TEX_W, TEX_H); // berth rail
        bodyModel[367].addBox(-42F, 54F, -4F, 4F, 1F, 23F, 0, 0.5F);
        bodyModel[368] = new ModelRendererTurbo(this, 1141, 242, TEX_W, TEX_H); // pillow
        bodyModel[368].addBox(-40F, 38F, 6F, 3F, 4F, 6F, 0, 0.5F);
        bodyModel[369] = new ModelRendererTurbo(this, 1810, 242, TEX_W, TEX_H); // table
        bodyModel[369].addBox(-16F, 32F, 14F, 14F, 2F, 6F, 0, 0.5F);
        bodyModel[370] = new ModelRendererTurbo(this, 2033, 210, TEX_W, TEX_H); // table leg
        bodyModel[370].addBox(-10F, 20F, 16F, 2F, 12F, 2F, 0, 0.5F);
        bodyModel[371] = new ModelRendererTurbo(this, 921, 264, TEX_W, TEX_H); // table lamp
        bodyModel[371].addBox(-12F, 34F, 16F, 2F, 4F, 2F, 0, 0.5F);
        bodyModel[372] = new ModelRendererTurbo(this, 1452, 152, TEX_W, TEX_H); // washstand
        bodyModel[372].addBox(-6F, 20F, 2F, 5F, 16F, 10F, 0, 0.5F);
        bodyModel[373] = new ModelRendererTurbo(this, 871, 242, TEX_W, TEX_H); // washstand top
        bodyModel[373].addBox(-6.5F, 36F, 1.5F, 5.5F, 1F, 11F, 0, 0.5F);
        bodyModel[374] = new ModelRendererTurbo(this, 1882, 184, TEX_W, TEX_H); // mirror frame
        bodyModel[374].addBox(-1.5F, 37.5F, 2.5F, 0.5F, 11F, 9F, 0, 0.5F);
        bodyModel[375] = new ModelRendererTurbo(this, 1964, 184, TEX_W, TEX_H); // mirror
        bodyModel[375].addBox(-2F, 38F, 3F, 0.5F, 10F, 8F, 0, 0.5F);
        bodyModel[376] = new ModelRendererTurbo(this, 1851, 242, TEX_W, TEX_H); // luggage rack
        bodyModel[376].addBox(-26F, 50F, -5F, 24F, 1F, 7F, 0, 0.5F);
        bodyModel[377] = new ModelRendererTurbo(this, 1878, 112, TEX_W, TEX_H); // sofa seat
        bodyModel[377].addBox(1F, 22F, -5F, 14F, 6F, 25F, 0, 0.5F);
        bodyModel[378] = new ModelRendererTurbo(this, 1141, 152, TEX_W, TEX_H); // sofa base
        bodyModel[378].addBox(1F, 20F, -5F, 14F, 2F, 25F, 0, 0.5F);
        bodyModel[379] = new ModelRendererTurbo(this, 1390, 112, TEX_W, TEX_H); // sofa front
        bodyModel[379].addBox(14.5F, 20F, -5F, 1F, 7F, 25F, 0, 0.5F);
        bodyModel[380] = new ModelRendererTurbo(this, 1620, 66, TEX_W, TEX_H); // sofa back
        bodyModel[380].addBox(1F, 28F, -5F, 4F, 16F, 25F, 0, 0.5F);
        bodyModel[381] = new ModelRendererTurbo(this, 1483, 152, TEX_W, TEX_H); // sofa back top
        bodyModel[381].addBox(1F, 44F, -5F, 5F, 1F, 25F, 0, 0.5F);
        bodyModel[382] = new ModelRendererTurbo(this, 930, 264, TEX_W, TEX_H); // armrest
        bodyModel[382].addBox(1F, 28F, -6F, 7F, 5F, 1F, 0, 0.5F);
        bodyModel[383] = new ModelRendererTurbo(this, 1957, 112, TEX_W, TEX_H); // upper berth, folded
        bodyModel[383].addBox(1F, 48F, -5F, 4F, 6F, 25F, 0, 0.5F);
        bodyModel[384] = new ModelRendererTurbo(this, 0, 152, TEX_W, TEX_H); // berth edge
        bodyModel[384].addBox(5F, 48F, -5F, 1F, 6F, 25F, 0, 0.5F);
        bodyModel[385] = new ModelRendererTurbo(this, 535, 184, TEX_W, TEX_H); // berth rail
        bodyModel[385].addBox(2F, 54F, -4F, 4F, 1F, 23F, 0, 0.5F);
        bodyModel[386] = new ModelRendererTurbo(this, 1160, 242, TEX_W, TEX_H); // pillow
        bodyModel[386].addBox(4F, 38F, 6F, 3F, 4F, 6F, 0, 0.5F);
        bodyModel[387] = new ModelRendererTurbo(this, 1914, 242, TEX_W, TEX_H); // table
        bodyModel[387].addBox(28F, 32F, 14F, 14F, 2F, 6F, 0, 0.5F);
        bodyModel[388] = new ModelRendererTurbo(this, 0, 227, TEX_W, TEX_H); // table leg
        bodyModel[388].addBox(34F, 20F, 16F, 2F, 12F, 2F, 0, 0.5F);
        bodyModel[389] = new ModelRendererTurbo(this, 947, 264, TEX_W, TEX_H); // table lamp
        bodyModel[389].addBox(32F, 34F, 16F, 2F, 4F, 2F, 0, 0.5F);
        bodyModel[390] = new ModelRendererTurbo(this, 1544, 152, TEX_W, TEX_H); // washstand
        bodyModel[390].addBox(38F, 20F, 2F, 5F, 16F, 10F, 0, 0.5F);
        bodyModel[391] = new ModelRendererTurbo(this, 904, 242, TEX_W, TEX_H); // washstand top
        bodyModel[391].addBox(37.5F, 36F, 1.5F, 5.5F, 1F, 11F, 0, 0.5F);
        bodyModel[392] = new ModelRendererTurbo(this, 1903, 184, TEX_W, TEX_H); // mirror frame
        bodyModel[392].addBox(42.5F, 37.5F, 2.5F, 0.5F, 11F, 9F, 0, 0.5F);
        bodyModel[393] = new ModelRendererTurbo(this, 1983, 184, TEX_W, TEX_H); // mirror
        bodyModel[393].addBox(42F, 38F, 3F, 0.5F, 10F, 8F, 0, 0.5F);
        bodyModel[394] = new ModelRendererTurbo(this, 1955, 242, TEX_W, TEX_H); // luggage rack
        bodyModel[394].addBox(18F, 50F, -5F, 24F, 1F, 7F, 0, 0.5F);
        bodyModel[395] = new ModelRendererTurbo(this, 53, 152, TEX_W, TEX_H); // sofa seat
        bodyModel[395].addBox(45F, 22F, -5F, 14F, 6F, 25F, 0, 0.5F);
        bodyModel[396] = new ModelRendererTurbo(this, 1220, 152, TEX_W, TEX_H); // sofa base
        bodyModel[396].addBox(45F, 20F, -5F, 14F, 2F, 25F, 0, 0.5F);
        bodyModel[397] = new ModelRendererTurbo(this, 1443, 112, TEX_W, TEX_H); // sofa front
        bodyModel[397].addBox(58.5F, 20F, -5F, 1F, 7F, 25F, 0, 0.5F);
        bodyModel[398] = new ModelRendererTurbo(this, 1679, 66, TEX_W, TEX_H); // sofa back
        bodyModel[398].addBox(45F, 28F, -5F, 4F, 16F, 25F, 0, 0.5F);
        bodyModel[399] = new ModelRendererTurbo(this, 1575, 152, TEX_W, TEX_H); // sofa back top
        bodyModel[399].addBox(45F, 44F, -5F, 5F, 1F, 25F, 0, 0.5F);
        bodyModel[400] = new ModelRendererTurbo(this, 956, 264, TEX_W, TEX_H); // armrest
        bodyModel[400].addBox(45F, 28F, -6F, 7F, 5F, 1F, 0, 0.5F);
        bodyModel[401] = new ModelRendererTurbo(this, 132, 152, TEX_W, TEX_H); // upper berth, folded
        bodyModel[401].addBox(45F, 48F, -5F, 4F, 6F, 25F, 0, 0.5F);
        bodyModel[402] = new ModelRendererTurbo(this, 191, 152, TEX_W, TEX_H); // berth edge
        bodyModel[402].addBox(49F, 48F, -5F, 1F, 6F, 25F, 0, 0.5F);
        bodyModel[403] = new ModelRendererTurbo(this, 590, 184, TEX_W, TEX_H); // berth rail
        bodyModel[403].addBox(46F, 54F, -4F, 4F, 1F, 23F, 0, 0.5F);
        bodyModel[404] = new ModelRendererTurbo(this, 1179, 242, TEX_W, TEX_H); // pillow
        bodyModel[404].addBox(48F, 38F, 6F, 3F, 4F, 6F, 0, 0.5F);
        bodyModel[405] = new ModelRendererTurbo(this, 0, 255, TEX_W, TEX_H); // table
        bodyModel[405].addBox(72F, 32F, 14F, 14F, 2F, 6F, 0, 0.5F);
        bodyModel[406] = new ModelRendererTurbo(this, 9, 227, TEX_W, TEX_H); // table leg
        bodyModel[406].addBox(78F, 20F, 16F, 2F, 12F, 2F, 0, 0.5F);
        bodyModel[407] = new ModelRendererTurbo(this, 973, 264, TEX_W, TEX_H); // table lamp
        bodyModel[407].addBox(76F, 34F, 16F, 2F, 4F, 2F, 0, 0.5F);
        bodyModel[408] = new ModelRendererTurbo(this, 1636, 152, TEX_W, TEX_H); // washstand
        bodyModel[408].addBox(82F, 20F, 2F, 5F, 16F, 10F, 0, 0.5F);
        bodyModel[409] = new ModelRendererTurbo(this, 937, 242, TEX_W, TEX_H); // washstand top
        bodyModel[409].addBox(81.5F, 36F, 1.5F, 5.5F, 1F, 11F, 0, 0.5F);
        bodyModel[410] = new ModelRendererTurbo(this, 1924, 184, TEX_W, TEX_H); // mirror frame
        bodyModel[410].addBox(86.5F, 37.5F, 2.5F, 0.5F, 11F, 9F, 0, 0.5F);
        bodyModel[411] = new ModelRendererTurbo(this, 2002, 184, TEX_W, TEX_H); // mirror
        bodyModel[411].addBox(86F, 38F, 3F, 0.5F, 10F, 8F, 0, 0.5F);
        bodyModel[412] = new ModelRendererTurbo(this, 41, 255, TEX_W, TEX_H); // luggage rack
        bodyModel[412].addBox(62F, 50F, -5F, 24F, 1F, 7F, 0, 0.5F);
        bodyModel[413] = new ModelRendererTurbo(this, 338, 282, TEX_W, TEX_H); // handrail
        bodyModel[413].addBox(-88F, 38F, -20F, 176F, 1F, 1F, 0, 0.5F);
        bodyModel[414] = new ModelRendererTurbo(this, 1140, 0, TEX_W, TEX_H); // ceiling
        bodyModel[414].addBox(-104F, 59F, -22F, 208F, 2F, 44F, 0, 0.5F);
        bodyModel[415] = new ModelRendererTurbo(this, 619, 0, TEX_W, TEX_H); // roof
        bodyModel[415].addBox(-106F, 61F, -24F, 212F, 1F, 48F, 0, 0.5F);
        bodyModel[416] = new ModelRendererTurbo(this, 0, 66, TEX_W, TEX_H); // roof
        bodyModel[416].addBox(-106F, 62F, -22F, 212F, 1F, 44F, 0, 0.5F);
        bodyModel[417] = new ModelRendererTurbo(this, 0, 112, TEX_W, TEX_H); // roof
        bodyModel[417].addBox(-106F, 63F, -19F, 212F, 1F, 38F, 0, 0.5F);
        bodyModel[418] = new ModelRendererTurbo(this, 244, 152, TEX_W, TEX_H); // roof
        bodyModel[418].addBox(-106F, 64F, -15F, 212F, 1F, 30F, 0, 0.5F);
        bodyModel[419] = new ModelRendererTurbo(this, 693, 282, TEX_W, TEX_H); // rain strip
        bodyModel[419].addBox(-106F, 61F, -25F, 212F, 1F, 1F, 0, 0.5F);
        bodyModel[420] = new ModelRendererTurbo(this, 1120, 282, TEX_W, TEX_H); // rain strip
        bodyModel[420].addBox(-106F, 61F, 24F, 212F, 1F, 1F, 0, 0.5F);
        bodyModel[421] = new ModelRendererTurbo(this, 150, 255, TEX_W, TEX_H); // clerestory side
        bodyModel[421].addBox(-92F, 65F, -10F, 184F, 6F, 1F, 0, 0.5F);
        bodyModel[422] = new ModelRendererTurbo(this, 521, 255, TEX_W, TEX_H); // clerestory side
        bodyModel[422].addBox(-92F, 65F, 9F, 184F, 6F, 1F, 0, 0.5F);
        bodyModel[423] = new ModelRendererTurbo(this, 645, 184, TEX_W, TEX_H); // clerestory inside
        bodyModel[423].addBox(-92F, 65F, -9F, 184F, 6F, 18F, 0, 0.5F);
        bodyModel[424] = new ModelRendererTurbo(this, 0, 184, TEX_W, TEX_H); // clerestory roof
        bodyModel[424].addBox(-94F, 71F, -12F, 188F, 1F, 24F, 0, 0.5F);
        bodyModel[425] = new ModelRendererTurbo(this, 1280, 184, TEX_W, TEX_H); // clerestory roof
        bodyModel[425].addBox(-94F, 72F, -10F, 188F, 1F, 20F, 0, 0.5F);
        bodyModel[426] = new ModelRendererTurbo(this, 1667, 152, TEX_W, TEX_H); // clerestory end
        bodyModel[426].addBox(-94F, 65F, -10F, 2F, 6F, 20F, 0, 0.5F);
        bodyModel[427] = new ModelRendererTurbo(this, 1712, 152, TEX_W, TEX_H); // clerestory end
        bodyModel[427].addBox(92F, 65F, -10F, 2F, 6F, 20F, 0, 0.5F);
        bodyModel[428] = new ModelRendererTurbo(this, 982, 264, TEX_W, TEX_H); // lamp vent
        bodyModel[428].addBox(-68F, 73F, -2F, 4F, 2F, 4F, 0, 0.5F);
        bodyModel[429] = new ModelRendererTurbo(this, 892, 255, TEX_W, TEX_H); // lamp vent cap
        bodyModel[429].addBox(-69F, 75F, -3F, 6F, 1F, 6F, 0, 0.5F);
        bodyModel[430] = new ModelRendererTurbo(this, 999, 264, TEX_W, TEX_H); // lamp vent
        bodyModel[430].addBox(-24F, 73F, -2F, 4F, 2F, 4F, 0, 0.5F);
        bodyModel[431] = new ModelRendererTurbo(this, 917, 255, TEX_W, TEX_H); // lamp vent cap
        bodyModel[431].addBox(-25F, 75F, -3F, 6F, 1F, 6F, 0, 0.5F);
        bodyModel[432] = new ModelRendererTurbo(this, 1016, 264, TEX_W, TEX_H); // lamp vent
        bodyModel[432].addBox(20F, 73F, -2F, 4F, 2F, 4F, 0, 0.5F);
        bodyModel[433] = new ModelRendererTurbo(this, 942, 255, TEX_W, TEX_H); // lamp vent cap
        bodyModel[433].addBox(19F, 75F, -3F, 6F, 1F, 6F, 0, 0.5F);
        bodyModel[434] = new ModelRendererTurbo(this, 1033, 264, TEX_W, TEX_H); // lamp vent
        bodyModel[434].addBox(64F, 73F, -2F, 4F, 2F, 4F, 0, 0.5F);
        bodyModel[435] = new ModelRendererTurbo(this, 967, 255, TEX_W, TEX_H); // lamp vent cap
        bodyModel[435].addBox(63F, 75F, -3F, 6F, 1F, 6F, 0, 0.5F);
    }
}
