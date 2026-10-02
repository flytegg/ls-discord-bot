package com.learnspigot.bot.index

import java.io.DataInputStream
import java.io.DataOutputStream

class IndexFile(var entries: Array<IndexEntry> = emptyArray()) {

    fun write(dos: DataOutputStream) {
        dos.writeInt(entries.size)

        for (entry in entries) {
            dos.writeUTF(entry.simpleName)
            dos.writeUTF(entry.name)
            dos.writeUTF(entry.url)
            dos.writeByte(entry.kind.ordinal)
        }
    }

    fun read(dis: DataInputStream,entrypoint: String) {
        val outp = arrayOfNulls<IndexEntry>(dis.readInt())

        for (i in 0 until outp.size) {
            outp[i]=IndexEntry(dis.readUTF(), dis.readUTF(), dis.readUTF(),
                IndexEntryKind.entries[dis.readByte().toInt()],entrypoint)
        }

        entries = outp as Array<IndexEntry>
    }

}