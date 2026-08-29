package io.yukkuric.hexjsneo.kubejs.probe

import io.yukkuric.hexjsneo.kubejs.sub.base.SingletonClassTracker
import io.yukkuric.hexjsneo.kubejs.sub.base.SubClassProvider
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin
import moe.wolfgirl.probejs.typescript.Documents
import moe.wolfgirl.probejs.typescript.document.Types
import moe.wolfgirl.probejs.typescript.document.members.FieldDecl

class PJSPluginHJS : ProbeJSPlugin() {
    override fun transformClass(document: Documents.ClassDocument) {
        val classInfo = document.classInfo()
        val classDocument = document.document()
        val obj = SingletonClassTracker.from(classInfo.clazz()) as? SubClassProvider ?: return
        for ((name, cls) in obj.contents()) {
            val field = FieldDecl(name, Types.typeOf(Types.clazz(cls)), false)
            classDocument.members.add(field)
        }
    }
}