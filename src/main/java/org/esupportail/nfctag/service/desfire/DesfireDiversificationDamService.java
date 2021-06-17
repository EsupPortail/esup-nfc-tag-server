package org.esupportail.nfctag.service.desfire;

import org.esupportail.nfctag.service.api.TagWriteApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;

import java.util.Random;

public class DesfireDiversificationDamService implements InitializingBean {

    protected final static Logger log = LoggerFactory.getLogger(DesfireDiversificationDamService.class);

    TagWriteApi tagWriteApi;

    DesfireDiversification desfireDiversification = new DesfireDiversification();

    public void setTagWriteApi(TagWriteApi tagWriteApi) {
        this.tagWriteApi = tagWriteApi;
    }

    public byte[] getDamAuthKey(String csn) throws Exception {
        byte[] cardUid = DesfireUtils.hexStringToByteArray(csn);
        
        String diversDamKey;
        diversDamKey = tagWriteApi.getDiversDamKey(csn);
        if (diversDamKey == null) {
            return new byte[16];
        }
        return desfireDiversification.diversificationAES128(DesfireUtils.hexStringToByteArray(diversDamKey), cardUid, cardUid.length);
    }

    public byte[] getDamEncKey(String csn) throws Exception {
        byte[] cardUid = DesfireUtils.hexStringToByteArray(csn);
        cardUid[0] = (byte) 0x00;

        String diversDamKey;
        diversDamKey = tagWriteApi.getDiversDamKey(csn);
        if (diversDamKey == null) {
            return new byte[16];
        }
        return desfireDiversification.diversificationAES128(DesfireUtils.hexStringToByteArray(diversDamKey), cardUid, cardUid.length);
    }

    public byte[] getDamMacKey(String csn) throws Exception {
        byte[] cardUid = DesfireUtils.hexStringToByteArray(csn);
        cardUid[0] = (byte) 0xFF;

        String diversDamKey;
        diversDamKey = tagWriteApi.getDiversDamKey(csn);
        if (diversDamKey == null) {
            return new byte[16];
        }
        return desfireDiversification.diversificationAES128(DesfireUtils.hexStringToByteArray(diversDamKey), cardUid, cardUid.length);
    }

    public String createDamBaseKey(String csn) {
        return tagWriteApi.createDiversDamKey(csn);
    }

    public String resetDamBaseKey(String csn) {
        return tagWriteApi.resetDiversDamKey(csn);
    }

    public byte[] calcEncK(byte[] piccDamEncKey, byte[] appDAMDefault, byte keyVerAppDAMDefault) throws Exception {

        byte[] IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        Random rand = new Random();
        byte[] random = new byte[7];
        for (int i = 0 ; i < random.length; i++) {
            random[i] = (byte) rand.nextInt(0xFF);
        }
        byte[] input = new byte[appDAMDefault.length + 16];

        System.arraycopy(random, 0, input, 0, 7);
        System.arraycopy(appDAMDefault, 0, input, 7, 16);
        input[input.length - 1] = keyVerAppDAMDefault;

        System.out.println("ENCK in " + DesfireUtils.byteArrayToHexString(input));
        System.out.println("ENCK in " + DesfireUtils.byteArrayToHexString(piccDamEncKey));
        System.out.println("ENCK in " + DesfireUtils.byteArrayToHexString(IV));

        byte[] EncK = desfireDiversification.encrypt(piccDamEncKey, input, IV);

        System.out.println("ENCK  out" + DesfireUtils.byteArrayToHexString(EncK));

        return EncK;
    }

    public byte[] calcDAMMAC(byte[] piccDamMacKey, byte cmd, int aid, int damSlotNo, byte damSlotVersion, int quotaLimit, byte key_setting_1, byte key_setting_2,
                             byte key_setting_3, byte aks_version, byte NoKeySets, byte MaxKeySize, byte Aks, int iso_df_id, byte[] iso_df_name, byte[] enck) throws Exception {

        byte[] IV = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
        byte[] input;
        int inputLength = 0;

        if ((key_setting_2 & 0x10) == 0x10) {
            inputLength++;
            if ((key_setting_3 & 0x01) == 0x01) {
                inputLength++;
                if ((NoKeySets >= 2) && (NoKeySets <= 16)) {
                    inputLength++;
                    if ((NoKeySets == 0x10) || (NoKeySets == 18)) {
                        inputLength++;
                    }
                    inputLength++;
                }
            }
        }

        if (iso_df_name != null)
            input = new byte[11 + enck.length + (iso_df_name.length + 2) + inputLength];
        else
            input = new byte[11 + enck.length + inputLength];

        inputLength = 0;
        input[inputLength++] = cmd;
        input[inputLength++] = (byte) (aid & 0x000000FF);
        input[inputLength++] = (byte) ((aid >> 8) & 0x00FF);
        input[inputLength++] = (byte) ((aid >> 16) & 0x00FF);
        input[inputLength++] = (byte) (damSlotNo & 0x00FF);
        input[inputLength++] = (byte) (damSlotNo >> 8);
        input[inputLength++] = damSlotVersion;
        input[inputLength++] = (byte) (quotaLimit & 0x00FF);
        input[inputLength++] = (byte) (quotaLimit >> 8);
        input[inputLength++] = key_setting_1;
        input[inputLength++] = key_setting_2;

        if ((key_setting_2 & 0x10) == 0x10) {
            input[inputLength++] = key_setting_3;
            if ((key_setting_3 & 0x01) == 0x01) {
                input[inputLength++] = aks_version;
                if ((NoKeySets >= 2) && (NoKeySets <= 16)) {
                    input[inputLength++] = NoKeySets;
                    if (NoKeySets == 0x10) {
                        input[inputLength++] = MaxKeySize;
                    }
                    input[inputLength++] = Aks;
                }
            }
        }

        if (iso_df_name != null) {
            input[inputLength++] = (byte) (iso_df_id & 0x00FF);
            input[inputLength++] = (byte) (iso_df_id >> 8);

            for (byte b : iso_df_name) input[inputLength++] = b;
        }
        /* add encK at the end */
        for (byte b : enck) input[inputLength++] = b;

        System.out.println("DAMMAC  in " + DesfireUtils.byteArrayToHexString(input));
        System.out.println("DAMMAC  PICCDAMMACKey " + DesfireUtils.byteArrayToHexString(piccDamMacKey));
        System.out.println("DAMMAC  IV " + DesfireUtils.byteArrayToHexString(IV));

        byte[] CMAC_enormous = this.CalculateCMAC(piccDamMacKey, IV, input);

        System.out.println("DAMMAC  out " + DesfireUtils.byteArrayToHexString(CMAC_enormous));

        System.out.println("CMAC_enormous calcul soft: " + DesfireUtils.byteArrayToHexString(CMAC_enormous));

        byte[] CMAC_full = new byte[16];
        System.arraycopy(CMAC_enormous, CMAC_enormous.length - 16, CMAC_full, 0, 16);

        System.out.println("CMAC_full calcul soft: " + DesfireUtils.byteArrayToHexString(CMAC_full));

        byte[] CMAC = new byte[8];
        int j = 0;

        for (int i = 1; i < CMAC_full.length; ) {
            CMAC[j++] = CMAC_full[i];
            i += 2;
        }

        System.out.println("CMAC calcul soft: " + DesfireUtils.byteArrayToHexString(CMAC));

        return CMAC;
    }

    public byte[] CalculateCMAC(byte[] Key, byte[] IV, byte[] input) throws Exception {

        // First : calculate subkey1 and subkey2
        byte[] Zeros = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};

        //byte[] K = { 0x2b, 0x7e, 0x15, 0x16, 0x28, 0xae, 0xd2, 0xa6, 0xab, 0xf7, 0x15, 0x88, 0x09, 0xcf, 0x4f, 0x3c } ;

        byte[] L = desfireDiversification.encrypt(Key, Zeros, IV);

        System.out.println(DesfireUtils.byteArrayToHexString(L));

        byte[] Key1;
        byte[] Key2;
        int i = 0;
        byte Rb = (byte) 0x87;
        byte MSB_L = L[0];
        int decal;

        // calcul de Key 1
        for (i = 0; i < L.length - 1; i++) {
            decal = (L[i] << 1);
            L[i] = (byte) (decal & 0x00FF);
            if ((L[i + 1] & 0x80) == 0x80) {
                L[i] |= 0x01;
            } else {
                L[i] |= 0x00;
            }
        }

        decal = (L[i] << 1);
        L[i] = (byte) (decal & 0x00FF);

        if (Byte.toUnsignedInt(MSB_L) >= (Byte.toUnsignedInt((byte) 0x80))) {
            L[L.length - 1] ^= Rb;
        }

        Key1 = L;

        System.out.println(DesfireUtils.byteArrayToHexString(Key1));

        byte[] tmp = new byte[Key1.length];
        System.arraycopy(Key1, 0, tmp, 0, Key1.length);

        // Calcul de key 2
        byte MSB_K1 = Key1[0];
        for (i = 0; i < L.length - 1; i++) {
            decal = (tmp[i] << 1);
            tmp[i] = (byte) (decal & 0x00FF);
            if ((tmp[i + 1] & 0x80) == 0x80) {
                tmp[i] |= 0x01;
            } else {
                tmp[i] |= 0x00;
            }
        }
        decal = (tmp[i] << 1);
        tmp[i] = (byte) (decal & 0x00FF);

        if (Byte.toUnsignedInt(MSB_K1) >= Byte.toUnsignedInt((byte) 0x80)) {
            tmp[tmp.length - 1] ^= Rb;
        }

        Key2 = tmp;

        System.out.println(DesfireUtils.byteArrayToHexString(Key2));

        byte[] result;

        /*-------------------------------------------------*/
        /* Cas 1 : la chaine est vide    */
        /* a- On concatene avec 0x80000000..00  (data) */
        /* b- on X-or avec Key2  (M1)*/
        /* c- on encrypte en AES-128 avec K et IV */
        /**/
        if (input == null) {
            byte[] data = {(byte) 0x80, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
            byte[] M1 = new byte[16];
            for (int k = 0; k < 16; k++)
                M1[k] = (byte) (data[k] ^ Key2[k]); // input

            result = desfireDiversification.encrypt(Key, M1, IV);

        } else {
            /**/

            /*--------------------------------------------------*/
            /* Cas 2 ! la chaine n'est pas vide et contient 16 octets  */
            /* a- on X-or avec Key 1 (data)  */
            /* b- on encrypte en AES-128 avec K et IV  */
            // byte[] data = { 0x6b, 0xc1, 0xbe, 0xe2, 0x2e, 0x40, 0x9f, 0x96, 0xe9, 0x3d, 0x7e, 0x11, 0x73, 0x93, 0x17, 0x2a };


            if (input.length == 16) {
                byte[] M1 = new byte[input.length];
                for (int k = 0; k < input.length; k++)
                    M1[k] = (byte) (input[k] ^ Key1[k]);

                result = desfireDiversification.encrypt(Key, M1, IV);
            } else {
                byte[] M = new byte[input.length + 16];
                int offset = 0;
                for (i = 0; i < input.length; i += 16) {
                    if ((i + 16) < input.length) {
                        /* block entier - on ne padde pas */
                        for (int j = 0; j < 16; j++)
                            M[offset++] = (byte) (input[i + j]);// ^ Key1[j]);

                    } else if ((i + 16) == input.length) {
                        /* block entier, on doit padder avec Key 1 */
                        for (int j = 0; j < 16; j++)
                            M[offset++] = (byte) (input[i + j] ^ Key1[j]);

                    } else {
                        /* block terminal */
                        byte remaining = (byte) (input.length - i);
                        byte NbPadd = (byte) (16 - remaining);


                        for (int j = 0; j < remaining; j++)
                            M[offset++] = (byte) (input[i + j] ^ Key2[j]);

                        byte key2_index_when_input_ends = (byte) (input.length % 16);
                        M[offset++] = (byte) (0x80 ^ Key2[key2_index_when_input_ends]);
                        NbPadd--;
                        key2_index_when_input_ends++;
                        for (int j = 1; j <= NbPadd; j++)
                            M[offset++] = Key2[remaining + j];

                    }

                }

                byte[] Message = new byte[offset];
                System.arraycopy(M, 0, Message, 0, offset);

                result = desfireDiversification.encrypt(Key, Message, IV);
            }
        }
        return result;
    }

    @Override
    public void afterPropertiesSet() {
        if (this.tagWriteApi == null) {
            throw new RuntimeException("WS url must be set");
        }
    }
}
