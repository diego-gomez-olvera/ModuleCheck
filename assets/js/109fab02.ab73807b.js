"use strict";(self.webpackChunkmodulecheck=self.webpackChunkmodulecheck||[]).push([[6668],{3905:function(e,n,t){t.d(n,{Zo:function(){return p},kt:function(){return m}});var r=t(67294);function a(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function o(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function i(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?o(Object(t),!0).forEach((function(n){a(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):o(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function s(e,n){if(null==e)return{};var t,r,a=function(e,n){if(null==e)return{};var t,r,a={},o=Object.keys(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||(a[t]=e[t]);return a}(e,n);if(Object.getOwnPropertySymbols){var o=Object.getOwnPropertySymbols(e);for(r=0;r<o.length;r++)t=o[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(a[t]=e[t])}return a}var l=r.createContext({}),u=function(e){var n=r.useContext(l),t=n;return e&&(t="function"==typeof e?e(n):i(i({},n),e)),t},p=function(e){var n=u(e.components);return r.createElement(l.Provider,{value:n},e.children)},c={inlineCode:"code",wrapper:function(e){var n=e.children;return r.createElement(r.Fragment,{},n)}},d=r.forwardRef((function(e,n){var t=e.components,a=e.mdxType,o=e.originalType,l=e.parentName,p=s(e,["components","mdxType","originalType","parentName"]),d=u(t),m=a,f=d["".concat(l,".").concat(m)]||d[m]||c[m]||o;return t?r.createElement(f,i(i({ref:n},p),{},{components:t})):r.createElement(f,i({ref:n},p))}));function m(e,n){var t=arguments,a=n&&n.mdxType;if("string"==typeof e||a){var o=t.length,i=new Array(o);i[0]=d;var s={};for(var l in n)hasOwnProperty.call(n,l)&&(s[l]=n[l]);s.originalType=e,s.mdxType="string"==typeof e?e:a,i[1]=s;for(var u=2;u<o;u++)i[u]=t[u];return r.createElement.apply(null,i)}return r.createElement.apply(null,t)}d.displayName="MDXCreateElement"},36323:function(e,n,t){t.r(n),t.d(n,{frontMatter:function(){return s},contentTitle:function(){return l},metadata:function(){return u},toc:function(){return p},default:function(){return d}});var r=t(83117),a=t(80102),o=(t(67294),t(3905)),i=["components"],s={id:"unused_kapt_processor",title:"Unused Kapt Processor",sidebar_label:"Unused Kapt Processor"},l=void 0,u={unversionedId:"rules/kapt/unused_kapt_processor",id:"version-0.11.0/rules/kapt/unused_kapt_processor",title:"Unused Kapt Processor",description:"Annotation processors act upon a defined set of annotations.  If an annotation processor is",source:"@site/versioned_docs/version-0.11.0/rules/kapt/unused_kapt_processor.md",sourceDirName:"rules/kapt",slug:"/rules/kapt/unused_kapt_processor",permalink:"/ModuleCheck/docs/0.11.0/rules/kapt/unused_kapt_processor",editUrl:"https://github.com/rbusarow/ModuleCheck/versioned_docs/version-0.11.0/rules/kapt/unused_kapt_processor.md",tags:[],version:"0.11.0",frontMatter:{id:"unused_kapt_processor",title:"Unused Kapt Processor",sidebar_label:"Unused Kapt Processor"},sidebar:"version-0.11.0/Docs",previous:{title:"Could Use Anvil Factory",permalink:"/ModuleCheck/docs/0.11.0/rules/compiler/could_use_anvil_factory"},next:{title:"Unused Kapt Plugin",permalink:"/ModuleCheck/docs/0.11.0/rules/kapt/unused_kapt_plugin"}},p=[],c={toc:p};function d(e){var n=e.components,t=(0,a.Z)(e,i);return(0,o.kt)("wrapper",(0,r.Z)({},c,t,{components:n,mdxType:"MDXLayout"}),(0,o.kt)("p",null,"Annotation processors act upon a defined set of annotations.  If an annotation processor is\nsufficiently popular and its api is stable, then it's relatively simple to define a list of\nannotations to search for.  For instance, Dagger looks for the following annotations:"),(0,o.kt)("ul",null,(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"javax.inject.Inject")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"dagger.Binds")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"dagger.Module")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"dagger.multibindings.IntoMap")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"dagger.multibindings.IntoSet")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"dagger.BindsInstance")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"dagger.Component")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"dagger.assisted.Assisted")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"dagger.assisted.AssistedInject")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"dagger.assisted.AssistedFactory")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"com.squareup.anvil.annotations.ContributesTo")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"com.squareup.anvil.annotations.MergeComponent")),(0,o.kt)("li",{parentName:"ul"},(0,o.kt)("inlineCode",{parentName:"li"},"com.squareup.anvil.annotations.MergeSubomponent"))),(0,o.kt)("p",null,"If a module has the Dagger ",(0,o.kt)("inlineCode",{parentName:"p"},"kapt")," dependency, and that module ",(0,o.kt)("em",{parentName:"p"},"does not")," have one of the above\nannotations somewhere, then Dagger isn't actually doing anything and can be removed."),(0,o.kt)("p",null,"This is simply a best-effort approach, and it isn't maintenance-free.  Over time, the list of\nannotations for any processor may change.  If this rule gives a false-positive finding because of a\nnew annotation, please open an issue and/or pull request."))}d.isMDXComponent=!0}}]);