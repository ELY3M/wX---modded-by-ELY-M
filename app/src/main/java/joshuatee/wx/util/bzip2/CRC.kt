/*
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2002 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "Ant" and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

/*
 * This package is based on the work done by Keiron Liddle, Aftex Software
 * <keiron@aftexsw.com> to whom the Ant project is very grateful for his
 * great code.
 */

package joshuatee.wx.util.bzip2

/**
 * A simple class the hold and calculate the CRC for sanity checking
 * of the data.
 *
 * @author [Keiron Liddle](mailto:keiron@aftexsw.com)
 */
class CRC {

    val finalCRC: Int
        get() = globalCRC.inv()

    private var globalCRC: Int = 0

    init {
        initialiseCRC()
    }

    fun initialiseCRC() {
        globalCRC = -0x1
    }

    fun updateCRC(inCh: Int) {
        var temp = globalCRC shr 24 xor inCh
        if (temp < 0) {
            temp += 256
        }
        globalCRC = globalCRC shl 8 xor CRC.crc32Table[temp]
    }

    companion object {
        var crc32Table: IntArray = intArrayOf(0x00000000, 0x04c11db7, 0x09823b6e, 0x0d4326d9, 0x130476dc, 0x17c56b6b, 0x1a864db2, 0x1e475005, 0x2608edb8, 0x22c9f00f, 0x2f8ad6d6, 0x2b4bcb61, 0x350c9b64, 0x31cd86d3, 0x3c8ea00a, 0x384fbdbd, 0x4c11db70, 0x48d0c6c7, 0x4593e01e, 0x4152fda9, 0x5f15adac, 0x5bd4b01b, 0x569796c2, 0x52568b75, 0x6a1936c8, 0x6ed82b7f, 0x639b0da6, 0x675a1011, 0x791d4014, 0x7ddc5da3, 0x709f7b7a, 0x745e66cd, -0x67dc4920, -0x631d54a9, -0x6e5e7272, -0x6a9f6fc7, -0x74d83fc4, -0x70192275, -0x7d5a04ae, -0x799b191b, -0x41d4a4a8, -0x4515b911, -0x48569fca, -0x4c97827f, -0x52d0d27c, -0x5611cfcd, -0x5b52e916, -0x5f93f4a3, -0x2bcd9270, -0x2f0c8fd9, -0x224fa902, -0x268eb4b7, -0x38c9e4b4, -0x3c08f905, -0x314bdfde, -0x358ac26b, -0xdc57fd8, -0x9046261, -0x44744ba, -0x86590f, -0x1ec1090c, -0x1a0014bd, -0x17433266, -0x13822fd3, 0x34867077, 0x30476dc0, 0x3d044b19, 0x39c556ae, 0x278206ab, 0x23431b1c, 0x2e003dc5, 0x2ac12072, 0x128e9dcf, 0x164f8078, 0x1b0ca6a1, 0x1fcdbb16, 0x018aeb13, 0x054bf6a4, 0x0808d07d, 0x0cc9cdca, 0x7897ab07, 0x7c56b6b0, 0x71159069, 0x75d48dde, 0x6b93dddb, 0x6f52c06c, 0x6211e6b5, 0x66d0fb02, 0x5e9f46bf, 0x5a5e5b08, 0x571d7dd1, 0x53dc6066, 0x4d9b3063, 0x495a2dd4, 0x44190b0d, 0x40d816ba, -0x535a3969, -0x579b24e0, -0x5ad80207, -0x5e191fb2, -0x405e4fb5, -0x449f5204, -0x49dc74db, -0x4d1d696e, -0x7552d4d1, -0x7193c968, -0x7cd0efbf, -0x7811f20a, -0x6656a20d, -0x6297bfbc, -0x6fd49963, -0x6b1584d6, -0x1f4be219, -0x1b8affb0, -0x16c9d977, -0x1208c4c2, -0xc4f94c5, -0x88e8974, -0x5cdafab, -0x10cb21e, -0x39430fa1, -0x3d821218, -0x30c134cf, -0x3400297a, -0x2a47797d, -0x2e8664cc, -0x23c54213, -0x27045fa6, 0x690ce0ee, 0x6dcdfd59, 0x608edb80, 0x644fc637, 0x7a089632, 0x7ec98b85, 0x738aad5c, 0x774bb0eb, 0x4f040d56, 0x4bc510e1, 0x46863638, 0x42472b8f, 0x5c007b8a, 0x58c1663d, 0x558240e4, 0x51435d53, 0x251d3b9e, 0x21dc2629, 0x2c9f00f0, 0x285e1d47, 0x36194d42, 0x32d850f5, 0x3f9b762c, 0x3b5a6b9b, 0x0315d626, 0x07d4cb91, 0x0a97ed48, 0x0e56f0ff, 0x1011a0fa, 0x14d0bd4d, 0x19939b94, 0x1d528623, -0xed0a9f2, -0xa11b447, -0x75292a0, -0x3938f29, -0x1dd4df2e, -0x1915c29b, -0x1456e444, -0x1097f9f5, -0x28d8444a, -0x2c1959ff, -0x215a7f28, -0x259b6291, -0x3bdc3296, -0x3f1d2f23, -0x325e09fc, -0x369f144d, -0x42c17282, -0x46006f37, -0x4b4349f0, -0x4f825459, -0x51c5045e, -0x550419eb, -0x58473f34, -0x5c862285, -0x64c99f3a, -0x6008828f, -0x6d4ba458, -0x698ab9e1, -0x77cde9e6, -0x730cf453, -0x7e4fd28c, -0x7a8ecf3d, 0x5d8a9099, 0x594b8d2e, 0x5408abf7, 0x50c9b640, 0x4e8ee645, 0x4a4ffbf2, 0x470cdd2b, 0x43cdc09c, 0x7b827d21, 0x7f436096, 0x7200464f, 0x76c15bf8, 0x68860bfd, 0x6c47164a, 0x61043093, 0x65c52d24, 0x119b4be9, 0x155a565e, 0x18197087, 0x1cd86d30, 0x029f3d35, 0x065e2082, 0x0b1d065b, 0x0fdc1bec, 0x3793a651, 0x3352bbe6, 0x3e119d3f, 0x3ad08088, 0x2497d08d, 0x2056cd3a, 0x2d15ebe3, 0x29d4f654, -0x3a56d987, -0x3e97c432, -0x33d4e2e9, -0x3715ff60, -0x2952af5b, -0x2d93b2ee, -0x20d09435, -0x24118984, -0x1c5e343f, -0x189f298a, -0x15dc0f51, -0x111d12e8, -0xf5a42e3, -0xb9b5f56, -0x6d8798d, -0x219643c, -0x764702f7, -0x72861f42, -0x7fc53999, -0x7b042430, -0x6543742b, -0x6182699e, -0x6cc14f45, -0x680052f4, -0x504fef4f, -0x548ef2fa, -0x59cdd421, -0x5d0cc998, -0x434b9993, -0x478a8426, -0x4ac9a2fd, -0x4e08bf4c)
    }
}


