package io.yukkuric.hexjsneo.kubejs.probe

import io.yukkuric.hexjsneo.kubejs.sub.base.SingletonClassTracker
import io.yukkuric.hexjsneo.kubejs.sub.base.SubClassProvider
import moe.wolfgirl.probejs.plugin.ProbeJSPlugin
import moe.wolfgirl.probejs.typescript.Documents
import moe.wolfgirl.probejs.typescript.document.Types
import moe.wolfgirl.probejs.typescript.document.members.ConstructorDecl
import moe.wolfgirl.probejs.typescript.document.members.FieldDecl

class PJSPluginHJS : ProbeJSPlugin() {
    override fun transformClass(document: Documents.ClassDocument) {
        val classInfo = document.classInfo()
        val classDocument = document.document()
        val targetCls = classInfo.clazz()

        // clean doc-gen class inners
        if (targetCls.packageName.startsWith("io.yukkuric.hexjsneo.kubejs.sub.base")) {
            classDocument.members.clear()
            return
        }

        // fetch saved fake singleton
        val obj = SingletonClassTracker.from(targetCls) as? SubClassProvider ?: return

        // remove inner stuff
        classDocument.members.removeIf rif@{
            if (it is FieldDecl) {
                if (it.isStatic) return@rif true
                if (it.name == "factory") return@rif true
            }
            if (it is ConstructorDecl) return@rif true
            false
        }

        // manual class dump
        for ((name, cls) in obj.contents()) {
            val field = FieldDecl(name, Types.typeOf(Types.clazz(cls)), false)
            classDocument.members.add(field)
        }
    }
}