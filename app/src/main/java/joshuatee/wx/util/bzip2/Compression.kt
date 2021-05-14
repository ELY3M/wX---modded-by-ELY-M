package joshuatee.wx.util.bzip2

import java.io.IOException
import java.io.InputStream
import java.util.zip.GZIPInputStream

/**
 * Characterises the compression status of a stream, and provides methods
 * for decompressing it.
 *
 * @author   Mark Taylor (Starlink)
 */
abstract class Compression
/**
 * Private sole constructor.
 *
 * @param   name  the name of this compression method
 */
private constructor(private val name: String) {

    /**
     * Returns a stream which is a decompressed version of the input stream,
     * according to this objects compression type.
     *
     * @param  raw  the raw input stream
     * @return  a stream giving the decompressed version of <tt>raw</tt>
     */
    @Throws(IOException::class)
    abstract fun decompress(raw: InputStream): InputStream

    /**
     * Returns the name of this compression type.
     *
     * @return  string representation
     */
    override fun toString() = name

    companion object {

        /** Number of bytes needed to determine compression type (magic number).  */
        private const val MAGIC_SIZE = 3

        /**
         * Returns a Compression object characterising the compression (if any)
         * represented by a given magic number.
         *
         * @param  magic  a buffer containing the first [.MAGIC_SIZE]
         * bytes of input of the stream to be characterised
         * @return  a <tt>Compression</tt> object of the type represented by
         * <tt>magic</tt>
         * @throws IllegalArgumentException  if <tt>magic.length&lt;MAGIC_SIZE</tt>
         */
        fun getCompression(magic: ByteArray): Compression = if (magic.size < MAGIC_SIZE) {
                throw IllegalArgumentException(
                    "Magic buffer must be at least MAGIC_SIZE=" + MAGIC_SIZE +
                            " bytes"
                )
            } else if (magic[0] == 0x1f.toByte() && magic[1] == 0x8b.toByte()) {
                GZIP
            } else if (magic[0] == 'B'.code.toByte() &&
                magic[1] == 'Z'.code.toByte() &&
                magic[2] == 'h'.code.toByte()
            ) {
                BZIP2
            } else if (magic[0] == 0x1f.toByte() && magic[1] == 0x9d.toByte()) {
                COMPRESS
            } else {
                NONE
            }

        /**
         * Returns a decompressed version of the given input stream.
         *
         * @return  the decompressed version of <tt>raw</tt>
         */
        /* @Throws(IOException::class)
         fun decompressStatic(raw: InputStream): InputStream {
             var raw = raw
             if (!raw.markSupported()) {
                 raw = BufferedInputStream(raw)
             }
             raw.mark(MAGIC_SIZE)
             val buf = ByteArray(MAGIC_SIZE)
             raw.read(buf)
             raw.reset()
             val compress = getCompression(buf)
             return compress.decompress(raw)
         }*/

        /**
         * A Compression object representing no compression (or perhaps an
         * unknown one).  The <tt>decompress</tt> method will return the
         * raw input stream unchanged.
         */
        private val NONE: Compression = object : Compression("none") {
            @Throws(IOException::class)
            override fun decompress(raw: InputStream): InputStream {
                //System.out.println("NO COMPRESSION FOUND");
                return raw
            }
        }

        /**
         * A Compression object representing GZip compression.
         */
        private val GZIP: Compression = object : Compression("gzip") {
            @Throws(IOException::class)
            override fun decompress(raw: InputStream): InputStream {
                //System.out.println("GZIP COMPRESSION FOUND");

                /* This is a workaround for a bug in GZIPInputStream in J2SE1.4.0
             * GZIPInputStream.markSupported() returns true; however
             * instances of this class do not support marking, which
             * screws up some things that the DataSource class tries to do.
             * So we fiddle the inflating stream to tell the truth. */
                /* Note this seems to be not uncommon in decompression streams
             * (had to fix the same bug in the UncompressInputStream
             * implementation used here too). */
                /* (bug ID 4812237 submitted to developer.java.sun.com by mbt) */
                return object : GZIPInputStream(raw) {
                    override fun markSupported(): Boolean = false
                }
            }
        }

        /**
         * A Compression object representing BZip2 compression.
         */
        private val BZIP2: Compression = object : Compression("bzip2") {
            @Throws(IOException::class)
            override fun decompress(raw: InputStream): InputStream {
                //System.out.println("BZIP2 COMPRESSION FOUND");
                /* Eat the first two bytes. */
                if (raw.read().toChar() != 'B' || raw.read().toChar() != 'Z') {
                    throw IllegalArgumentException(
                        "Wrong magic number for bzip2 encoding"
                    )
                }
                return CBZip2InputStream(raw)
            }
        }

        /**
         * A Compression object representing Unix compress-type compression.
         */
        private val COMPRESS: Compression = object : Compression("compress") {
            @Throws(IOException::class)
            override fun decompress(raw: InputStream): InputStream = UncompressInputStream(raw)
        }
    }
}

