package train.render.models;

import fexcraft.tmt.slim.ModelBase;
import fexcraft.tmt.slim.ModelRendererTurbo;

/**
 * A luxury sleeping car of the early 1900s (made for this port by tools/gen_lux_sleeper.py): navy and gold, a clerestory
 * roof, a corridor along one side, four sleeping compartments (a sofa by day, the upper berth folded up, a table
 * by the window, a washstand), vestibules with doors at both ends. x along, y up, z across, in pixels.
 */
public class ModelPassengerLuxSleeper extends ModelBase {
    static final int TEX_W = 512, TEX_H = 768;

    public ModelPassengerLuxSleeper() {
        bodyModel = new ModelRendererTurbo[260];

        bodyModel[0] = new ModelRendererTurbo(this, 165, 201, TEX_W, TEX_H); // wheel
        bodyModel[0].addBox(-43F, 0F, -5.5F, 6F, 6F, 1F);
        bodyModel[1] = new ModelRendererTurbo(this, 202, 233, TEX_W, TEX_H); // wheel
        bodyModel[1].addBox(-43F, 0F, 4.5F, 6F, 6F, 1F);
        bodyModel[2] = new ModelRendererTurbo(this, 24, 148, TEX_W, TEX_H); // axle
        bodyModel[2].addBox(-40.5F, 2.5F, -4.5F, 1F, 1F, 9F);
        bodyModel[3] = new ModelRendererTurbo(this, 420, 226, TEX_W, TEX_H); // wheel
        bodyModel[3].addBox(-31F, 0F, -5.5F, 6F, 6F, 1F);
        bodyModel[4] = new ModelRendererTurbo(this, 48, 215, TEX_W, TEX_H); // wheel
        bodyModel[4].addBox(-31F, 0F, 4.5F, 6F, 6F, 1F);
        bodyModel[5] = new ModelRendererTurbo(this, 298, 147, TEX_W, TEX_H); // axle
        bodyModel[5].addBox(-28.5F, 2.5F, -4.5F, 1F, 1F, 9F);
        bodyModel[6] = new ModelRendererTurbo(this, 465, 176, TEX_W, TEX_H); // bogie side frame
        bodyModel[6].addBox(-45F, 2F, -7F, 22F, 3F, 1F);
        bodyModel[7] = new ModelRendererTurbo(this, 109, 722, TEX_W, TEX_H); // axlebox spring
        bodyModel[7].addBox(-36F, 1F, -7.2F, 4F, 2F, 1F);
        bodyModel[8] = new ModelRendererTurbo(this, 44, 171, TEX_W, TEX_H); // bogie side frame
        bodyModel[8].addBox(-45F, 2F, 6F, 22F, 3F, 1F);
        bodyModel[9] = new ModelRendererTurbo(this, 214, 724, TEX_W, TEX_H); // axlebox spring
        bodyModel[9].addBox(-36F, 1F, 6.2F, 4F, 2F, 1F);
        bodyModel[10] = new ModelRendererTurbo(this, 123, 149, TEX_W, TEX_H); // bolster
        bodyModel[10].addBox(-36F, 5F, -7F, 4F, 2F, 14F);
        bodyModel[11] = new ModelRendererTurbo(this, 282, 219, TEX_W, TEX_H); // wheel
        bodyModel[11].addBox(25F, 0F, -5.5F, 6F, 6F, 1F);
        bodyModel[12] = new ModelRendererTurbo(this, 30, 228, TEX_W, TEX_H); // wheel
        bodyModel[12].addBox(25F, 0F, 4.5F, 6F, 6F, 1F);
        bodyModel[13] = new ModelRendererTurbo(this, 63, 158, TEX_W, TEX_H); // axle
        bodyModel[13].addBox(27.5F, 2.5F, -4.5F, 1F, 1F, 9F);
        bodyModel[14] = new ModelRendererTurbo(this, 322, 232, TEX_W, TEX_H); // wheel
        bodyModel[14].addBox(37F, 0F, -5.5F, 6F, 6F, 1F);
        bodyModel[15] = new ModelRendererTurbo(this, 298, 195, TEX_W, TEX_H); // wheel
        bodyModel[15].addBox(37F, 0F, 4.5F, 6F, 6F, 1F);
        bodyModel[16] = new ModelRendererTurbo(this, 295, 181, TEX_W, TEX_H); // axle
        bodyModel[16].addBox(39.5F, 2.5F, -4.5F, 1F, 1F, 9F);
        bodyModel[17] = new ModelRendererTurbo(this, 203, 147, TEX_W, TEX_H); // bogie side frame
        bodyModel[17].addBox(23F, 2F, -7F, 22F, 3F, 1F);
        bodyModel[18] = new ModelRendererTurbo(this, 499, 734, TEX_W, TEX_H); // axlebox spring
        bodyModel[18].addBox(32F, 1F, -7.2F, 4F, 2F, 1F);
        bodyModel[19] = new ModelRendererTurbo(this, 23, 179, TEX_W, TEX_H); // bogie side frame
        bodyModel[19].addBox(23F, 2F, 6F, 22F, 3F, 1F);
        bodyModel[20] = new ModelRendererTurbo(this, 439, 728, TEX_W, TEX_H); // axlebox spring
        bodyModel[20].addBox(32F, 1F, 6.2F, 4F, 2F, 1F);
        bodyModel[21] = new ModelRendererTurbo(this, 148, 170, TEX_W, TEX_H); // bolster
        bodyModel[21].addBox(32F, 5F, -7F, 4F, 2F, 14F);
        bodyModel[22] = new ModelRendererTurbo(this, 73, 161, TEX_W, TEX_H); // underframe
        bodyModel[22].addBox(-52F, 6F, -9F, 104F, 1F, 18F);
        bodyModel[23] = new ModelRendererTurbo(this, 60, 180, TEX_W, TEX_H); // solebar
        bodyModel[23].addBox(-52F, 6F, -10.5F, 104F, 2F, 1F);
        bodyModel[24] = new ModelRendererTurbo(this, 157, 179, TEX_W, TEX_H); // solebar
        bodyModel[24].addBox(-52F, 6F, 9.5F, 104F, 2F, 1F);
        bodyModel[25] = new ModelRendererTurbo(this, 417, 155, TEX_W, TEX_H); // battery box
        bodyModel[25].addBox(-14F, 3F, -6F, 8F, 3F, 12F);
        bodyModel[26] = new ModelRendererTurbo(this, 52, 162, TEX_W, TEX_H); // buffer beam
        bodyModel[26].addBox(-53F, 6F, -11F, 1F, 3F, 22F);
        bodyModel[27] = new ModelRendererTurbo(this, 292, 760, TEX_W, TEX_H); // buffer
        bodyModel[27].addBox(-55F, 6.5F, -8F, 2F, 2F, 2F);
        bodyModel[28] = new ModelRendererTurbo(this, 96, 743, TEX_W, TEX_H); // buffer
        bodyModel[28].addBox(-55F, 6.5F, 6F, 2F, 2F, 2F);
        bodyModel[29] = new ModelRendererTurbo(this, 49, 755, TEX_W, TEX_H); // coupling hook
        bodyModel[29].addBox(-54F, 7F, -0.5F, 1F, 1F, 1F);
        bodyModel[30] = new ModelRendererTurbo(this, 364, 148, TEX_W, TEX_H); // battery box
        bodyModel[30].addBox(6F, 3F, -6F, 8F, 3F, 12F);
        bodyModel[31] = new ModelRendererTurbo(this, 288, 145, TEX_W, TEX_H); // buffer beam
        bodyModel[31].addBox(52F, 6F, -11F, 1F, 3F, 22F);
        bodyModel[32] = new ModelRendererTurbo(this, 316, 733, TEX_W, TEX_H); // buffer
        bodyModel[32].addBox(53F, 6.5F, -8F, 2F, 2F, 2F);
        bodyModel[33] = new ModelRendererTurbo(this, 254, 763, TEX_W, TEX_H); // buffer
        bodyModel[33].addBox(53F, 6.5F, 6F, 2F, 2F, 2F);
        bodyModel[34] = new ModelRendererTurbo(this, 272, 747, TEX_W, TEX_H); // coupling hook
        bodyModel[34].addBox(53F, 7F, -0.5F, 1F, 1F, 1F);
        bodyModel[35] = new ModelRendererTurbo(this, 160, 158, TEX_W, TEX_H); // floor frame
        bodyModel[35].addBox(-52F, 8F, -11F, 104F, 1F, 22F);
        bodyModel[36] = new ModelRendererTurbo(this, 232, 347, TEX_W, TEX_H); // carpet
        bodyModel[36].addBox(-44F, 9F, -10F, 88F, 1F, 20F);
        bodyModel[37] = new ModelRendererTurbo(this, 153, 295, TEX_W, TEX_H); // vestibule floor
        bodyModel[37].addBox(-52F, 9F, -10F, 8F, 1F, 20F);
        bodyModel[38] = new ModelRendererTurbo(this, 406, 155, TEX_W, TEX_H); // step
        bodyModel[38].addBox(-51F, 5F, -12.5F, 6F, 1F, 2F);
        bodyModel[39] = new ModelRendererTurbo(this, 357, 159, TEX_W, TEX_H); // step
        bodyModel[39].addBox(-51F, 5F, 10.5F, 6F, 1F, 2F);
        bodyModel[40] = new ModelRendererTurbo(this, 41, 306, TEX_W, TEX_H); // vestibule floor
        bodyModel[40].addBox(44F, 9F, -10F, 8F, 1F, 20F);
        bodyModel[41] = new ModelRendererTurbo(this, 153, 177, TEX_W, TEX_H); // step
        bodyModel[41].addBox(45F, 5F, -12.5F, 6F, 1F, 2F);
        bodyModel[42] = new ModelRendererTurbo(this, 253, 165, TEX_W, TEX_H); // step
        bodyModel[42].addBox(45F, 5F, 10.5F, 6F, 1F, 2F);
        bodyModel[43] = new ModelRendererTurbo(this, 229, 18, TEX_W, TEX_H);
        bodyModel[43].addBox(-52F, 10F, 10.5F, 104F, 5F, 0.5F);
        bodyModel[44] = new ModelRendererTurbo(this, 37, 55, TEX_W, TEX_H); // gold line
        bodyModel[44].addBox(-52F, 15F, 11F, 104F, 1F, 0.25F);
        bodyModel[45] = new ModelRendererTurbo(this, 262, 26, TEX_W, TEX_H);
        bodyModel[45].addBox(-52F, 15F, 10.5F, 104F, 1F, 0.5F);
        bodyModel[46] = new ModelRendererTurbo(this, 84, 21, TEX_W, TEX_H);
        bodyModel[46].addBox(-52F, 23F, 10.5F, 104F, 4F, 0.5F);
        bodyModel[47] = new ModelRendererTurbo(this, 77, 79, TEX_W, TEX_H); // gold line
        bodyModel[47].addBox(-52F, 27F, 11F, 104F, 1F, 0.25F);
        bodyModel[48] = new ModelRendererTurbo(this, 215, 674, TEX_W, TEX_H);
        bodyModel[48].addBox(-52F, 27F, 10.5F, 104F, 1F, 0.5F);
        bodyModel[49] = new ModelRendererTurbo(this, 39, 275, TEX_W, TEX_H); // lining
        bodyModel[49].addBox(-44F, 10F, 10F, 88F, 6F, 0.5F);
        bodyModel[50] = new ModelRendererTurbo(this, 293, 260, TEX_W, TEX_H); // lining
        bodyModel[50].addBox(-44F, 23F, 10F, 88F, 5F, 0.5F);
        bodyModel[51] = new ModelRendererTurbo(this, 174, 22, TEX_W, TEX_H);
        bodyModel[51].addBox(-44F, 16F, 10.5F, 3F, 7F, 0.5F);
        bodyModel[52] = new ModelRendererTurbo(this, 304, 271, TEX_W, TEX_H);
        bodyModel[52].addBox(-44F, 16F, 10F, 3F, 7F, 0.5F);
        bodyModel[53] = new ModelRendererTurbo(this, 296, 29, TEX_W, TEX_H);
        bodyModel[53].addBox(-31F, 16F, 10.5F, 2F, 7F, 0.5F);
        bodyModel[54] = new ModelRendererTurbo(this, 35, 245, TEX_W, TEX_H);
        bodyModel[54].addBox(-31F, 16F, 10F, 2F, 7F, 0.5F);
        bodyModel[55] = new ModelRendererTurbo(this, 483, 17, TEX_W, TEX_H);
        bodyModel[55].addBox(-25F, 16F, 10.5F, 6F, 7F, 0.5F);
        bodyModel[56] = new ModelRendererTurbo(this, 242, 244, TEX_W, TEX_H);
        bodyModel[56].addBox(-25F, 16F, 10F, 6F, 7F, 0.5F);
        bodyModel[57] = new ModelRendererTurbo(this, 31, 19, TEX_W, TEX_H);
        bodyModel[57].addBox(-9F, 16F, 10.5F, 2F, 7F, 0.5F);
        bodyModel[58] = new ModelRendererTurbo(this, 331, 276, TEX_W, TEX_H);
        bodyModel[58].addBox(-9F, 16F, 10F, 2F, 7F, 0.5F);
        bodyModel[59] = new ModelRendererTurbo(this, 348, 28, TEX_W, TEX_H);
        bodyModel[59].addBox(-3F, 16F, 10.5F, 6F, 7F, 0.5F);
        bodyModel[60] = new ModelRendererTurbo(this, 145, 264, TEX_W, TEX_H);
        bodyModel[60].addBox(-3F, 16F, 10F, 6F, 7F, 0.5F);
        bodyModel[61] = new ModelRendererTurbo(this, 454, 22, TEX_W, TEX_H);
        bodyModel[61].addBox(13F, 16F, 10.5F, 2F, 7F, 0.5F);
        bodyModel[62] = new ModelRendererTurbo(this, 11, 269, TEX_W, TEX_H);
        bodyModel[62].addBox(13F, 16F, 10F, 2F, 7F, 0.5F);
        bodyModel[63] = new ModelRendererTurbo(this, 181, 10, TEX_W, TEX_H);
        bodyModel[63].addBox(19F, 16F, 10.5F, 6F, 7F, 0.5F);
        bodyModel[64] = new ModelRendererTurbo(this, 312, 247, TEX_W, TEX_H);
        bodyModel[64].addBox(19F, 16F, 10F, 6F, 7F, 0.5F);
        bodyModel[65] = new ModelRendererTurbo(this, 252, 3, TEX_W, TEX_H);
        bodyModel[65].addBox(35F, 16F, 10.5F, 2F, 7F, 0.5F);
        bodyModel[66] = new ModelRendererTurbo(this, 111, 258, TEX_W, TEX_H);
        bodyModel[66].addBox(35F, 16F, 10F, 2F, 7F, 0.5F);
        bodyModel[67] = new ModelRendererTurbo(this, 66, 15, TEX_W, TEX_H);
        bodyModel[67].addBox(41F, 16F, 10.5F, 3F, 7F, 0.5F);
        bodyModel[68] = new ModelRendererTurbo(this, 203, 265, TEX_W, TEX_H);
        bodyModel[68].addBox(41F, 16F, 10F, 3F, 7F, 0.5F);
        bodyModel[69] = new ModelRendererTurbo(this, 469, 319, TEX_W, TEX_H); // sill
        bodyModel[69].addBox(-41F, 15.5F, 9.5F, 10F, 0.5F, 0.5F);
        bodyModel[70] = new ModelRendererTurbo(this, 41, 298, TEX_W, TEX_H); // sill
        bodyModel[70].addBox(-29F, 15.5F, 9.5F, 4F, 0.5F, 0.5F);
        bodyModel[71] = new ModelRendererTurbo(this, 229, 313, TEX_W, TEX_H); // sill
        bodyModel[71].addBox(-19F, 15.5F, 9.5F, 10F, 0.5F, 0.5F);
        bodyModel[72] = new ModelRendererTurbo(this, 281, 305, TEX_W, TEX_H); // sill
        bodyModel[72].addBox(-7F, 15.5F, 9.5F, 4F, 0.5F, 0.5F);
        bodyModel[73] = new ModelRendererTurbo(this, 452, 296, TEX_W, TEX_H); // sill
        bodyModel[73].addBox(3F, 15.5F, 9.5F, 10F, 0.5F, 0.5F);
        bodyModel[74] = new ModelRendererTurbo(this, 419, 315, TEX_W, TEX_H); // sill
        bodyModel[74].addBox(15F, 15.5F, 9.5F, 4F, 0.5F, 0.5F);
        bodyModel[75] = new ModelRendererTurbo(this, 442, 323, TEX_W, TEX_H); // sill
        bodyModel[75].addBox(25F, 15.5F, 9.5F, 10F, 0.5F, 0.5F);
        bodyModel[76] = new ModelRendererTurbo(this, 142, 333, TEX_W, TEX_H); // sill
        bodyModel[76].addBox(37F, 15.5F, 9.5F, 4F, 0.5F, 0.5F);
        bodyModel[77] = new ModelRendererTurbo(this, 212, 22, TEX_W, TEX_H);
        bodyModel[77].addBox(-45F, 16F, 10.5F, 1F, 7F, 0.5F);
        bodyModel[78] = new ModelRendererTurbo(this, 349, 24, TEX_W, TEX_H);
        bodyModel[78].addBox(-52F, 16F, 10.5F, 1F, 7F, 0.5F);
        bodyModel[79] = new ModelRendererTurbo(this, 490, 14, TEX_W, TEX_H);
        bodyModel[79].addBox(-51F, 10F, 10.5F, 6F, 6F, 0.5F);
        bodyModel[80] = new ModelRendererTurbo(this, 77, 5, TEX_W, TEX_H);
        bodyModel[80].addBox(-51F, 16F, 10.5F, 1.5F, 7F, 0.5F);
        bodyModel[81] = new ModelRendererTurbo(this, 90, 9, TEX_W, TEX_H);
        bodyModel[81].addBox(-46.5F, 16F, 10.5F, 1.5F, 7F, 0.5F);
        bodyModel[82] = new ModelRendererTurbo(this, 118, 446, TEX_W, TEX_H); // door window
        bodyModel[82].addBox(-49.5F, 16F, 10.6F, 3F, 7F, 0.2F);
        bodyModel[83] = new ModelRendererTurbo(this, 6, 559, TEX_W, TEX_H); // door handle
        bodyModel[83].addBox(-47F, 13F, 11F, 1F, 1F, 0.5F);
        bodyModel[84] = new ModelRendererTurbo(this, 425, 37, TEX_W, TEX_H);
        bodyModel[84].addBox(44F, 16F, 10.5F, 1F, 7F, 0.5F);
        bodyModel[85] = new ModelRendererTurbo(this, 93, 16, TEX_W, TEX_H);
        bodyModel[85].addBox(51F, 16F, 10.5F, 1F, 7F, 0.5F);
        bodyModel[86] = new ModelRendererTurbo(this, 144, 0, TEX_W, TEX_H);
        bodyModel[86].addBox(45F, 10F, 10.5F, 6F, 6F, 0.5F);
        bodyModel[87] = new ModelRendererTurbo(this, 74, 26, TEX_W, TEX_H);
        bodyModel[87].addBox(45F, 16F, 10.5F, 1.5F, 7F, 0.5F);
        bodyModel[88] = new ModelRendererTurbo(this, 273, 23, TEX_W, TEX_H);
        bodyModel[88].addBox(49.5F, 16F, 10.5F, 1.5F, 7F, 0.5F);
        bodyModel[89] = new ModelRendererTurbo(this, 312, 468, TEX_W, TEX_H); // door window
        bodyModel[89].addBox(46.5F, 16F, 10.6F, 3F, 7F, 0.2F);
        bodyModel[90] = new ModelRendererTurbo(this, 163, 536, TEX_W, TEX_H); // door handle
        bodyModel[90].addBox(49F, 13F, 11F, 1F, 1F, 0.5F);
        bodyModel[91] = new ModelRendererTurbo(this, 263, 39, TEX_W, TEX_H);
        bodyModel[91].addBox(-52F, 10F, -11F, 104F, 5F, 0.5F);
        bodyModel[92] = new ModelRendererTurbo(this, 27, 77, TEX_W, TEX_H); // gold line
        bodyModel[92].addBox(-52F, 15F, -11.25F, 104F, 1F, 0.25F);
        bodyModel[93] = new ModelRendererTurbo(this, 286, 25, TEX_W, TEX_H);
        bodyModel[93].addBox(-52F, 15F, -11F, 104F, 1F, 0.5F);
        bodyModel[94] = new ModelRendererTurbo(this, 203, 25, TEX_W, TEX_H);
        bodyModel[94].addBox(-52F, 23F, -11F, 104F, 4F, 0.5F);
        bodyModel[95] = new ModelRendererTurbo(this, 201, 54, TEX_W, TEX_H); // gold line
        bodyModel[95].addBox(-52F, 27F, -11.25F, 104F, 1F, 0.25F);
        bodyModel[96] = new ModelRendererTurbo(this, 246, 712, TEX_W, TEX_H);
        bodyModel[96].addBox(-52F, 27F, -11F, 104F, 1F, 0.5F);
        bodyModel[97] = new ModelRendererTurbo(this, 205, 243, TEX_W, TEX_H); // lining
        bodyModel[97].addBox(-44F, 10F, -10.5F, 88F, 6F, 0.5F);
        bodyModel[98] = new ModelRendererTurbo(this, 97, 244, TEX_W, TEX_H); // lining
        bodyModel[98].addBox(-44F, 23F, -10.5F, 88F, 5F, 0.5F);
        bodyModel[99] = new ModelRendererTurbo(this, 504, 13, TEX_W, TEX_H);
        bodyModel[99].addBox(-44F, 16F, -11F, 2F, 7F, 0.5F);
        bodyModel[100] = new ModelRendererTurbo(this, 225, 250, TEX_W, TEX_H);
        bodyModel[100].addBox(-44F, 16F, -10.5F, 2F, 7F, 0.5F);
        bodyModel[101] = new ModelRendererTurbo(this, 56, 21, TEX_W, TEX_H);
        bodyModel[101].addBox(-35F, 16F, -11F, 4F, 7F, 0.5F);
        bodyModel[102] = new ModelRendererTurbo(this, 307, 243, TEX_W, TEX_H);
        bodyModel[102].addBox(-35F, 16F, -10.5F, 4F, 7F, 0.5F);
        bodyModel[103] = new ModelRendererTurbo(this, 52, 0, TEX_W, TEX_H);
        bodyModel[103].addBox(-24F, 16F, -11F, 4F, 7F, 0.5F);
        bodyModel[104] = new ModelRendererTurbo(this, 290, 249, TEX_W, TEX_H);
        bodyModel[104].addBox(-24F, 16F, -10.5F, 4F, 7F, 0.5F);
        bodyModel[105] = new ModelRendererTurbo(this, 274, 6, TEX_W, TEX_H);
        bodyModel[105].addBox(-13F, 16F, -11F, 4F, 7F, 0.5F);
        bodyModel[106] = new ModelRendererTurbo(this, 485, 263, TEX_W, TEX_H);
        bodyModel[106].addBox(-13F, 16F, -10.5F, 4F, 7F, 0.5F);
        bodyModel[107] = new ModelRendererTurbo(this, 314, 1, TEX_W, TEX_H);
        bodyModel[107].addBox(-2F, 16F, -11F, 4F, 7F, 0.5F);
        bodyModel[108] = new ModelRendererTurbo(this, 36, 253, TEX_W, TEX_H);
        bodyModel[108].addBox(-2F, 16F, -10.5F, 4F, 7F, 0.5F);
        bodyModel[109] = new ModelRendererTurbo(this, 314, 24, TEX_W, TEX_H);
        bodyModel[109].addBox(9F, 16F, -11F, 4F, 7F, 0.5F);
        bodyModel[110] = new ModelRendererTurbo(this, 76, 280, TEX_W, TEX_H);
        bodyModel[110].addBox(9F, 16F, -10.5F, 4F, 7F, 0.5F);
        bodyModel[111] = new ModelRendererTurbo(this, 129, 22, TEX_W, TEX_H);
        bodyModel[111].addBox(20F, 16F, -11F, 4F, 7F, 0.5F);
        bodyModel[112] = new ModelRendererTurbo(this, 308, 263, TEX_W, TEX_H);
        bodyModel[112].addBox(20F, 16F, -10.5F, 4F, 7F, 0.5F);
        bodyModel[113] = new ModelRendererTurbo(this, 242, 7, TEX_W, TEX_H);
        bodyModel[113].addBox(31F, 16F, -11F, 4F, 7F, 0.5F);
        bodyModel[114] = new ModelRendererTurbo(this, 59, 271, TEX_W, TEX_H);
        bodyModel[114].addBox(31F, 16F, -10.5F, 4F, 7F, 0.5F);
        bodyModel[115] = new ModelRendererTurbo(this, 500, 29, TEX_W, TEX_H);
        bodyModel[115].addBox(42F, 16F, -11F, 2F, 7F, 0.5F);
        bodyModel[116] = new ModelRendererTurbo(this, 245, 270, TEX_W, TEX_H);
        bodyModel[116].addBox(42F, 16F, -10.5F, 2F, 7F, 0.5F);
        bodyModel[117] = new ModelRendererTurbo(this, 159, 293, TEX_W, TEX_H); // sill
        bodyModel[117].addBox(-42F, 15.5F, -10F, 7F, 0.5F, 0.5F);
        bodyModel[118] = new ModelRendererTurbo(this, 73, 294, TEX_W, TEX_H); // sill
        bodyModel[118].addBox(-31F, 15.5F, -10F, 7F, 0.5F, 0.5F);
        bodyModel[119] = new ModelRendererTurbo(this, 383, 309, TEX_W, TEX_H); // sill
        bodyModel[119].addBox(-20F, 15.5F, -10F, 7F, 0.5F, 0.5F);
        bodyModel[120] = new ModelRendererTurbo(this, 379, 304, TEX_W, TEX_H); // sill
        bodyModel[120].addBox(-9F, 15.5F, -10F, 7F, 0.5F, 0.5F);
        bodyModel[121] = new ModelRendererTurbo(this, 245, 332, TEX_W, TEX_H); // sill
        bodyModel[121].addBox(2F, 15.5F, -10F, 7F, 0.5F, 0.5F);
        bodyModel[122] = new ModelRendererTurbo(this, 82, 321, TEX_W, TEX_H); // sill
        bodyModel[122].addBox(13F, 15.5F, -10F, 7F, 0.5F, 0.5F);
        bodyModel[123] = new ModelRendererTurbo(this, 11, 301, TEX_W, TEX_H); // sill
        bodyModel[123].addBox(24F, 15.5F, -10F, 7F, 0.5F, 0.5F);
        bodyModel[124] = new ModelRendererTurbo(this, 486, 321, TEX_W, TEX_H); // sill
        bodyModel[124].addBox(35F, 15.5F, -10F, 7F, 0.5F, 0.5F);
        bodyModel[125] = new ModelRendererTurbo(this, 185, 9, TEX_W, TEX_H);
        bodyModel[125].addBox(-45F, 16F, -11F, 1F, 7F, 0.5F);
        bodyModel[126] = new ModelRendererTurbo(this, 353, 34, TEX_W, TEX_H);
        bodyModel[126].addBox(-52F, 16F, -11F, 1F, 7F, 0.5F);
        bodyModel[127] = new ModelRendererTurbo(this, 468, 1, TEX_W, TEX_H);
        bodyModel[127].addBox(-51F, 10F, -11F, 6F, 6F, 0.5F);
        bodyModel[128] = new ModelRendererTurbo(this, 388, 33, TEX_W, TEX_H);
        bodyModel[128].addBox(-51F, 16F, -11F, 1.5F, 7F, 0.5F);
        bodyModel[129] = new ModelRendererTurbo(this, 152, 5, TEX_W, TEX_H);
        bodyModel[129].addBox(-46.5F, 16F, -11F, 1.5F, 7F, 0.5F);
        bodyModel[130] = new ModelRendererTurbo(this, 356, 448, TEX_W, TEX_H); // door window
        bodyModel[130].addBox(-49.5F, 16F, -10.8F, 3F, 7F, 0.2F);
        bodyModel[131] = new ModelRendererTurbo(this, 265, 551, TEX_W, TEX_H); // door handle
        bodyModel[131].addBox(-47F, 13F, -11.5F, 1F, 1F, 0.5F);
        bodyModel[132] = new ModelRendererTurbo(this, 465, 10, TEX_W, TEX_H);
        bodyModel[132].addBox(44F, 16F, -11F, 1F, 7F, 0.5F);
        bodyModel[133] = new ModelRendererTurbo(this, 182, 14, TEX_W, TEX_H);
        bodyModel[133].addBox(51F, 16F, -11F, 1F, 7F, 0.5F);
        bodyModel[134] = new ModelRendererTurbo(this, 272, 34, TEX_W, TEX_H);
        bodyModel[134].addBox(45F, 10F, -11F, 6F, 6F, 0.5F);
        bodyModel[135] = new ModelRendererTurbo(this, 398, 32, TEX_W, TEX_H);
        bodyModel[135].addBox(45F, 16F, -11F, 1.5F, 7F, 0.5F);
        bodyModel[136] = new ModelRendererTurbo(this, 168, 40, TEX_W, TEX_H);
        bodyModel[136].addBox(49.5F, 16F, -11F, 1.5F, 7F, 0.5F);
        bodyModel[137] = new ModelRendererTurbo(this, 114, 471, TEX_W, TEX_H); // door window
        bodyModel[137].addBox(46.5F, 16F, -10.8F, 3F, 7F, 0.2F);
        bodyModel[138] = new ModelRendererTurbo(this, 415, 540, TEX_W, TEX_H); // door handle
        bodyModel[138].addBox(49F, 13F, -11.5F, 1F, 1F, 0.5F);
        bodyModel[139] = new ModelRendererTurbo(this, 412, 7, TEX_W, TEX_H); // end wall
        bodyModel[139].addBox(-52F, 10F, -11F, 1F, 19F, 8F);
        bodyModel[140] = new ModelRendererTurbo(this, 418, 12, TEX_W, TEX_H); // end wall
        bodyModel[140].addBox(-52F, 10F, 3F, 1F, 19F, 8F);
        bodyModel[141] = new ModelRendererTurbo(this, 378, 14, TEX_W, TEX_H); // over the gangway door
        bodyModel[141].addBox(-52F, 26F, -3F, 1F, 3F, 6F);
        bodyModel[142] = new ModelRendererTurbo(this, 102, 160, TEX_W, TEX_H); // bellows
        bodyModel[142].addBox(-53F, 10F, -5F, 1F, 17F, 2F);
        bodyModel[143] = new ModelRendererTurbo(this, 252, 155, TEX_W, TEX_H); // bellows
        bodyModel[143].addBox(-53F, 10F, 3F, 1F, 17F, 2F);
        bodyModel[144] = new ModelRendererTurbo(this, 374, 145, TEX_W, TEX_H); // bellows
        bodyModel[144].addBox(-53F, 26F, -3F, 1F, 1F, 6F);
        bodyModel[145] = new ModelRendererTurbo(this, 14, 8, TEX_W, TEX_H); // end wall
        bodyModel[145].addBox(51F, 10F, -11F, 1F, 19F, 8F);
        bodyModel[146] = new ModelRendererTurbo(this, 241, 8, TEX_W, TEX_H); // end wall
        bodyModel[146].addBox(51F, 10F, 3F, 1F, 19F, 8F);
        bodyModel[147] = new ModelRendererTurbo(this, 99, 38, TEX_W, TEX_H); // over the gangway door
        bodyModel[147].addBox(51F, 26F, -3F, 1F, 3F, 6F);
        bodyModel[148] = new ModelRendererTurbo(this, 489, 155, TEX_W, TEX_H); // bellows
        bodyModel[148].addBox(52F, 10F, -5F, 1F, 17F, 2F);
        bodyModel[149] = new ModelRendererTurbo(this, 228, 169, TEX_W, TEX_H); // bellows
        bodyModel[149].addBox(52F, 10F, 3F, 1F, 17F, 2F);
        bodyModel[150] = new ModelRendererTurbo(this, 479, 166, TEX_W, TEX_H); // bellows
        bodyModel[150].addBox(52F, 26F, -3F, 1F, 1F, 6F);
        bodyModel[151] = new ModelRendererTurbo(this, 186, 242, TEX_W, TEX_H); // corridor wall
        bodyModel[151].addBox(-44F, 10F, -4F, 15F, 18F, 1F);
        bodyModel[152] = new ModelRendererTurbo(this, 112, 243, TEX_W, TEX_H); // corridor wall
        bodyModel[152].addBox(-24F, 10F, -4F, 2F, 18F, 1F);
        bodyModel[153] = new ModelRendererTurbo(this, 116, 270, TEX_W, TEX_H); // over the door
        bodyModel[153].addBox(-29F, 25F, -4F, 5F, 3F, 1F);
        bodyModel[154] = new ModelRendererTurbo(this, 100, 549, TEX_W, TEX_H); // door frame
        bodyModel[154].addBox(-29F, 10F, -4.5F, 0.5F, 15F, 2F);
        bodyModel[155] = new ModelRendererTurbo(this, 104, 558, TEX_W, TEX_H); // door frame
        bodyModel[155].addBox(-24.5F, 10F, -4.5F, 0.5F, 15F, 2F);
        bodyModel[156] = new ModelRendererTurbo(this, 319, 268, TEX_W, TEX_H); // corridor wall
        bodyModel[156].addBox(-22F, 10F, -4F, 15F, 18F, 1F);
        bodyModel[157] = new ModelRendererTurbo(this, 312, 266, TEX_W, TEX_H); // corridor wall
        bodyModel[157].addBox(-2F, 10F, -4F, 2F, 18F, 1F);
        bodyModel[158] = new ModelRendererTurbo(this, 0, 270, TEX_W, TEX_H); // over the door
        bodyModel[158].addBox(-7F, 25F, -4F, 5F, 3F, 1F);
        bodyModel[159] = new ModelRendererTurbo(this, 465, 550, TEX_W, TEX_H); // door frame
        bodyModel[159].addBox(-7F, 10F, -4.5F, 0.5F, 15F, 2F);
        bodyModel[160] = new ModelRendererTurbo(this, 409, 533, TEX_W, TEX_H); // door frame
        bodyModel[160].addBox(-2.5F, 10F, -4.5F, 0.5F, 15F, 2F);
        bodyModel[161] = new ModelRendererTurbo(this, 427, 261, TEX_W, TEX_H); // corridor wall
        bodyModel[161].addBox(0F, 10F, -4F, 15F, 18F, 1F);
        bodyModel[162] = new ModelRendererTurbo(this, 61, 269, TEX_W, TEX_H); // corridor wall
        bodyModel[162].addBox(20F, 10F, -4F, 2F, 18F, 1F);
        bodyModel[163] = new ModelRendererTurbo(this, 198, 252, TEX_W, TEX_H); // over the door
        bodyModel[163].addBox(15F, 25F, -4F, 5F, 3F, 1F);
        bodyModel[164] = new ModelRendererTurbo(this, 244, 539, TEX_W, TEX_H); // door frame
        bodyModel[164].addBox(15F, 10F, -4.5F, 0.5F, 15F, 2F);
        bodyModel[165] = new ModelRendererTurbo(this, 222, 549, TEX_W, TEX_H); // door frame
        bodyModel[165].addBox(19.5F, 10F, -4.5F, 0.5F, 15F, 2F);
        bodyModel[166] = new ModelRendererTurbo(this, 44, 265, TEX_W, TEX_H); // corridor wall
        bodyModel[166].addBox(22F, 10F, -4F, 15F, 18F, 1F);
        bodyModel[167] = new ModelRendererTurbo(this, 484, 263, TEX_W, TEX_H); // corridor wall
        bodyModel[167].addBox(42F, 10F, -4F, 2F, 18F, 1F);
        bodyModel[168] = new ModelRendererTurbo(this, 202, 269, TEX_W, TEX_H); // over the door
        bodyModel[168].addBox(37F, 25F, -4F, 5F, 3F, 1F);
        bodyModel[169] = new ModelRendererTurbo(this, 205, 533, TEX_W, TEX_H); // door frame
        bodyModel[169].addBox(37F, 10F, -4.5F, 0.5F, 15F, 2F);
        bodyModel[170] = new ModelRendererTurbo(this, 371, 538, TEX_W, TEX_H); // door frame
        bodyModel[170].addBox(41.5F, 10F, -4.5F, 0.5F, 15F, 2F);
        bodyModel[171] = new ModelRendererTurbo(this, 87, 244, TEX_W, TEX_H); // partition
        bodyModel[171].addBox(-22.5F, 10F, -3F, 1F, 18F, 13F);
        bodyModel[172] = new ModelRendererTurbo(this, 14, 244, TEX_W, TEX_H); // partition
        bodyModel[172].addBox(-0.5F, 10F, -3F, 1F, 18F, 13F);
        bodyModel[173] = new ModelRendererTurbo(this, 302, 254, TEX_W, TEX_H); // partition
        bodyModel[173].addBox(21.5F, 10F, -3F, 1F, 18F, 13F);
        bodyModel[174] = new ModelRendererTurbo(this, 412, 244, TEX_W, TEX_H); // end partition
        bodyModel[174].addBox(-44.5F, 10F, -3F, 1F, 18F, 13F);
        bodyModel[175] = new ModelRendererTurbo(this, 313, 278, TEX_W, TEX_H); // over the corridor end
        bodyModel[175].addBox(-44.5F, 25F, -10F, 1F, 3F, 6F);
        bodyModel[176] = new ModelRendererTurbo(this, 501, 255, TEX_W, TEX_H); // corner
        bodyModel[176].addBox(-44.5F, 10F, -4F, 1F, 18F, 1F);
        bodyModel[177] = new ModelRendererTurbo(this, 336, 251, TEX_W, TEX_H); // end partition
        bodyModel[177].addBox(43.5F, 10F, -3F, 1F, 18F, 13F);
        bodyModel[178] = new ModelRendererTurbo(this, 79, 275, TEX_W, TEX_H); // over the corridor end
        bodyModel[178].addBox(43.5F, 25F, -10F, 1F, 3F, 6F);
        bodyModel[179] = new ModelRendererTurbo(this, 280, 244, TEX_W, TEX_H); // corner
        bodyModel[179].addBox(43.5F, 10F, -4F, 1F, 18F, 1F);
        bodyModel[180] = new ModelRendererTurbo(this, 10, 384, TEX_W, TEX_H); // sofa seat
        bodyModel[180].addBox(-43.5F, 10F, -2.5F, 7F, 4F, 12.5F);
        bodyModel[181] = new ModelRendererTurbo(this, 409, 294, TEX_W, TEX_H); // sofa front
        bodyModel[181].addBox(-37F, 10F, -2.5F, 0.5F, 3.5F, 12.5F);
        bodyModel[182] = new ModelRendererTurbo(this, 269, 407, TEX_W, TEX_H); // sofa back
        bodyModel[182].addBox(-43.5F, 14F, -2.5F, 2F, 8F, 12.5F);
        bodyModel[183] = new ModelRendererTurbo(this, 478, 296, TEX_W, TEX_H); // armrest
        bodyModel[183].addBox(-43.5F, 14F, -3F, 3F, 3F, 0.5F);
        bodyModel[184] = new ModelRendererTurbo(this, 222, 588, TEX_W, TEX_H); // upper berth, folded
        bodyModel[184].addBox(-43.5F, 24F, -2.5F, 2F, 3F, 12.5F);
        bodyModel[185] = new ModelRendererTurbo(this, 422, 301, TEX_W, TEX_H); // berth edge
        bodyModel[185].addBox(-41.5F, 24F, -2.5F, 0.5F, 3F, 12.5F);
        bodyModel[186] = new ModelRendererTurbo(this, 14, 304, TEX_W, TEX_H); // table
        bodyModel[186].addBox(-30F, 16F, 7F, 7F, 1F, 3F);
        bodyModel[187] = new ModelRendererTurbo(this, 108, 546, TEX_W, TEX_H); // table leg
        bodyModel[187].addBox(-27F, 10F, 8F, 1F, 6F, 1F);
        bodyModel[188] = new ModelRendererTurbo(this, 256, 303, TEX_W, TEX_H); // washstand
        bodyModel[188].addBox(-25F, 10F, 1F, 2.5F, 8F, 5F);
        bodyModel[189] = new ModelRendererTurbo(this, 391, 661, TEX_W, TEX_H); // washstand top
        bodyModel[189].addBox(-25F, 18F, 1F, 2.5F, 0.5F, 5F);
        bodyModel[190] = new ModelRendererTurbo(this, 166, 448, TEX_W, TEX_H); // mirror
        bodyModel[190].addBox(-23F, 19F, 1.5F, 0.5F, 5F, 4F);
        bodyModel[191] = new ModelRendererTurbo(this, 278, 410, TEX_W, TEX_H); // sofa seat
        bodyModel[191].addBox(-21.5F, 10F, -2.5F, 7F, 4F, 12.5F);
        bodyModel[192] = new ModelRendererTurbo(this, 427, 296, TEX_W, TEX_H); // sofa front
        bodyModel[192].addBox(-15F, 10F, -2.5F, 0.5F, 3.5F, 12.5F);
        bodyModel[193] = new ModelRendererTurbo(this, 31, 407, TEX_W, TEX_H); // sofa back
        bodyModel[193].addBox(-21.5F, 14F, -2.5F, 2F, 8F, 12.5F);
        bodyModel[194] = new ModelRendererTurbo(this, 181, 317, TEX_W, TEX_H); // armrest
        bodyModel[194].addBox(-21.5F, 14F, -3F, 3F, 3F, 0.5F);
        bodyModel[195] = new ModelRendererTurbo(this, 339, 602, TEX_W, TEX_H); // upper berth, folded
        bodyModel[195].addBox(-21.5F, 24F, -2.5F, 2F, 3F, 12.5F);
        bodyModel[196] = new ModelRendererTurbo(this, 423, 320, TEX_W, TEX_H); // berth edge
        bodyModel[196].addBox(-19.5F, 24F, -2.5F, 0.5F, 3F, 12.5F);
        bodyModel[197] = new ModelRendererTurbo(this, 66, 322, TEX_W, TEX_H); // table
        bodyModel[197].addBox(-8F, 16F, 7F, 7F, 1F, 3F);
        bodyModel[198] = new ModelRendererTurbo(this, 77, 561, TEX_W, TEX_H); // table leg
        bodyModel[198].addBox(-5F, 10F, 8F, 1F, 6F, 1F);
        bodyModel[199] = new ModelRendererTurbo(this, 261, 289, TEX_W, TEX_H); // washstand
        bodyModel[199].addBox(-3F, 10F, 1F, 2.5F, 8F, 5F);
        bodyModel[200] = new ModelRendererTurbo(this, 446, 652, TEX_W, TEX_H); // washstand top
        bodyModel[200].addBox(-3F, 18F, 1F, 2.5F, 0.5F, 5F);
        bodyModel[201] = new ModelRendererTurbo(this, 397, 443, TEX_W, TEX_H); // mirror
        bodyModel[201].addBox(-1F, 19F, 1.5F, 0.5F, 5F, 4F);
        bodyModel[202] = new ModelRendererTurbo(this, 311, 384, TEX_W, TEX_H); // sofa seat
        bodyModel[202].addBox(0.5F, 10F, -2.5F, 7F, 4F, 12.5F);
        bodyModel[203] = new ModelRendererTurbo(this, 397, 297, TEX_W, TEX_H); // sofa front
        bodyModel[203].addBox(7F, 10F, -2.5F, 0.5F, 3.5F, 12.5F);
        bodyModel[204] = new ModelRendererTurbo(this, 88, 388, TEX_W, TEX_H); // sofa back
        bodyModel[204].addBox(0.5F, 14F, -2.5F, 2F, 8F, 12.5F);
        bodyModel[205] = new ModelRendererTurbo(this, 242, 327, TEX_W, TEX_H); // armrest
        bodyModel[205].addBox(0.5F, 14F, -3F, 3F, 3F, 0.5F);
        bodyModel[206] = new ModelRendererTurbo(this, 371, 583, TEX_W, TEX_H); // upper berth, folded
        bodyModel[206].addBox(0.5F, 24F, -2.5F, 2F, 3F, 12.5F);
        bodyModel[207] = new ModelRendererTurbo(this, 284, 291, TEX_W, TEX_H); // berth edge
        bodyModel[207].addBox(2.5F, 24F, -2.5F, 0.5F, 3F, 12.5F);
        bodyModel[208] = new ModelRendererTurbo(this, 166, 331, TEX_W, TEX_H); // table
        bodyModel[208].addBox(14F, 16F, 7F, 7F, 1F, 3F);
        bodyModel[209] = new ModelRendererTurbo(this, 265, 561, TEX_W, TEX_H); // table leg
        bodyModel[209].addBox(17F, 10F, 8F, 1F, 6F, 1F);
        bodyModel[210] = new ModelRendererTurbo(this, 284, 318, TEX_W, TEX_H); // washstand
        bodyModel[210].addBox(19F, 10F, 1F, 2.5F, 8F, 5F);
        bodyModel[211] = new ModelRendererTurbo(this, 401, 630, TEX_W, TEX_H); // washstand top
        bodyModel[211].addBox(19F, 18F, 1F, 2.5F, 0.5F, 5F);
        bodyModel[212] = new ModelRendererTurbo(this, 452, 467, TEX_W, TEX_H); // mirror
        bodyModel[212].addBox(21F, 19F, 1.5F, 0.5F, 5F, 4F);
        bodyModel[213] = new ModelRendererTurbo(this, 29, 399, TEX_W, TEX_H); // sofa seat
        bodyModel[213].addBox(22.5F, 10F, -2.5F, 7F, 4F, 12.5F);
        bodyModel[214] = new ModelRendererTurbo(this, 97, 305, TEX_W, TEX_H); // sofa front
        bodyModel[214].addBox(29F, 10F, -2.5F, 0.5F, 3.5F, 12.5F);
        bodyModel[215] = new ModelRendererTurbo(this, 21, 408, TEX_W, TEX_H); // sofa back
        bodyModel[215].addBox(22.5F, 14F, -2.5F, 2F, 8F, 12.5F);
        bodyModel[216] = new ModelRendererTurbo(this, 50, 320, TEX_W, TEX_H); // armrest
        bodyModel[216].addBox(22.5F, 14F, -3F, 3F, 3F, 0.5F);
        bodyModel[217] = new ModelRendererTurbo(this, 231, 577, TEX_W, TEX_H); // upper berth, folded
        bodyModel[217].addBox(22.5F, 24F, -2.5F, 2F, 3F, 12.5F);
        bodyModel[218] = new ModelRendererTurbo(this, 389, 292, TEX_W, TEX_H); // berth edge
        bodyModel[218].addBox(24.5F, 24F, -2.5F, 0.5F, 3F, 12.5F);
        bodyModel[219] = new ModelRendererTurbo(this, 226, 308, TEX_W, TEX_H); // table
        bodyModel[219].addBox(36F, 16F, 7F, 7F, 1F, 3F);
        bodyModel[220] = new ModelRendererTurbo(this, 313, 560, TEX_W, TEX_H); // table leg
        bodyModel[220].addBox(39F, 10F, 8F, 1F, 6F, 1F);
        bodyModel[221] = new ModelRendererTurbo(this, 310, 320, TEX_W, TEX_H); // washstand
        bodyModel[221].addBox(41F, 10F, 1F, 2.5F, 8F, 5F);
        bodyModel[222] = new ModelRendererTurbo(this, 102, 641, TEX_W, TEX_H); // washstand top
        bodyModel[222].addBox(41F, 18F, 1F, 2.5F, 0.5F, 5F);
        bodyModel[223] = new ModelRendererTurbo(this, 231, 464, TEX_W, TEX_H); // mirror
        bodyModel[223].addBox(43F, 19F, 1.5F, 0.5F, 5F, 4F);
        bodyModel[224] = new ModelRendererTurbo(this, 273, 558, TEX_W, TEX_H); // handrail
        bodyModel[224].addBox(-44F, 19F, -10F, 88F, 0.5F, 0.5F);
        bodyModel[225] = new ModelRendererTurbo(this, 259, 487, TEX_W, TEX_H); // ceiling
        bodyModel[225].addBox(-52F, 28F, -11F, 104F, 1F, 22F);
        bodyModel[226] = new ModelRendererTurbo(this, 178, 112, TEX_W, TEX_H); // roof
        bodyModel[226].addBox(-53F, 29F, -12F, 106F, 1F, 24F);
        bodyModel[227] = new ModelRendererTurbo(this, 132, 125, TEX_W, TEX_H); // roof
        bodyModel[227].addBox(-53F, 30F, -9F, 106F, 1F, 18F);
        bodyModel[228] = new ModelRendererTurbo(this, 286, 684, TEX_W, TEX_H); // clerestory side
        bodyModel[228].addBox(-46F, 31F, -5F, 92F, 3F, 1F);
        bodyModel[229] = new ModelRendererTurbo(this, 229, 680, TEX_W, TEX_H); // clerestory side
        bodyModel[229].addBox(-46F, 31F, 4F, 92F, 3F, 1F);
        bodyModel[230] = new ModelRendererTurbo(this, 213, 439, TEX_W, TEX_H); // clerestory window
        bodyModel[230].addBox(-42F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[231] = new ModelRendererTurbo(this, 200, 460, TEX_W, TEX_H); // clerestory window
        bodyModel[231].addBox(-42F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[232] = new ModelRendererTurbo(this, 161, 436, TEX_W, TEX_H); // clerestory window
        bodyModel[232].addBox(-33.6F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[233] = new ModelRendererTurbo(this, 343, 447, TEX_W, TEX_H); // clerestory window
        bodyModel[233].addBox(-33.6F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[234] = new ModelRendererTurbo(this, 219, 436, TEX_W, TEX_H); // clerestory window
        bodyModel[234].addBox(-25.2F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[235] = new ModelRendererTurbo(this, 108, 474, TEX_W, TEX_H); // clerestory window
        bodyModel[235].addBox(-25.2F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[236] = new ModelRendererTurbo(this, 155, 439, TEX_W, TEX_H); // clerestory window
        bodyModel[236].addBox(-16.8F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[237] = new ModelRendererTurbo(this, 459, 441, TEX_W, TEX_H); // clerestory window
        bodyModel[237].addBox(-16.8F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[238] = new ModelRendererTurbo(this, 481, 477, TEX_W, TEX_H); // clerestory window
        bodyModel[238].addBox(-8.4F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[239] = new ModelRendererTurbo(this, 329, 474, TEX_W, TEX_H); // clerestory window
        bodyModel[239].addBox(-8.4F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[240] = new ModelRendererTurbo(this, 187, 441, TEX_W, TEX_H); // clerestory window
        bodyModel[240].addBox(0F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[241] = new ModelRendererTurbo(this, 129, 440, TEX_W, TEX_H); // clerestory window
        bodyModel[241].addBox(0F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[242] = new ModelRendererTurbo(this, 495, 461, TEX_W, TEX_H); // clerestory window
        bodyModel[242].addBox(8.4F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[243] = new ModelRendererTurbo(this, 112, 438, TEX_W, TEX_H); // clerestory window
        bodyModel[243].addBox(8.4F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[244] = new ModelRendererTurbo(this, 203, 463, TEX_W, TEX_H); // clerestory window
        bodyModel[244].addBox(16.8F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[245] = new ModelRendererTurbo(this, 83, 474, TEX_W, TEX_H); // clerestory window
        bodyModel[245].addBox(16.8F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[246] = new ModelRendererTurbo(this, 426, 446, TEX_W, TEX_H); // clerestory window
        bodyModel[246].addBox(25.2F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[247] = new ModelRendererTurbo(this, 82, 477, TEX_W, TEX_H); // clerestory window
        bodyModel[247].addBox(25.2F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[248] = new ModelRendererTurbo(this, 220, 464, TEX_W, TEX_H); // clerestory window
        bodyModel[248].addBox(33.6F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[249] = new ModelRendererTurbo(this, 206, 453, TEX_W, TEX_H); // clerestory window
        bodyModel[249].addBox(33.6F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[250] = new ModelRendererTurbo(this, 215, 444, TEX_W, TEX_H); // clerestory window
        bodyModel[250].addBox(42F, 31.75F, 5F, 4F, 1.5F, 0.2F);
        bodyModel[251] = new ModelRendererTurbo(this, 182, 452, TEX_W, TEX_H); // clerestory window
        bodyModel[251].addBox(42F, 31.75F, -5.2F, 4F, 1.5F, 0.2F);
        bodyModel[252] = new ModelRendererTurbo(this, 47, 503, TEX_W, TEX_H); // clerestory inside
        bodyModel[252].addBox(-46F, 31F, -4F, 92F, 3F, 8F);
        bodyModel[253] = new ModelRendererTurbo(this, 9, 117, TEX_W, TEX_H); // clerestory roof
        bodyModel[253].addBox(-47F, 34F, -6F, 94F, 1F, 12F);
        bodyModel[254] = new ModelRendererTurbo(this, 283, 125, TEX_W, TEX_H); // clerestory end
        bodyModel[254].addBox(-47F, 31F, -5F, 1F, 3F, 10F);
        bodyModel[255] = new ModelRendererTurbo(this, 225, 97, TEX_W, TEX_H); // clerestory end
        bodyModel[255].addBox(46F, 31F, -5F, 1F, 3F, 10F);
        bodyModel[256] = new ModelRendererTurbo(this, 196, 741, TEX_W, TEX_H); // vent
        bodyModel[256].addBox(-34F, 35F, -1F, 2F, 1F, 2F);
        bodyModel[257] = new ModelRendererTurbo(this, 264, 759, TEX_W, TEX_H); // vent
        bodyModel[257].addBox(-12F, 35F, -1F, 2F, 1F, 2F);
        bodyModel[258] = new ModelRendererTurbo(this, 151, 752, TEX_W, TEX_H); // vent
        bodyModel[258].addBox(10F, 35F, -1F, 2F, 1F, 2F);
        bodyModel[259] = new ModelRendererTurbo(this, 491, 724, TEX_W, TEX_H); // vent
        bodyModel[259].addBox(32F, 35F, -1F, 2F, 1F, 2F);
    }
}
