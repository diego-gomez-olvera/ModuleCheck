"use strict";(self.webpackChunkmodulecheck=self.webpackChunkmodulecheck||[]).push([[6768],{3905:function(e,n,t){t.d(n,{Zo:function(){return c},kt:function(){return f}});var r=t(67294);function a(e,n,t){return n in e?Object.defineProperty(e,n,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[n]=t,e}function i(e,n){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var r=Object.getOwnPropertySymbols(e);n&&(r=r.filter((function(n){return Object.getOwnPropertyDescriptor(e,n).enumerable}))),t.push.apply(t,r)}return t}function o(e){for(var n=1;n<arguments.length;n++){var t=null!=arguments[n]?arguments[n]:{};n%2?i(Object(t),!0).forEach((function(n){a(e,n,t[n])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):i(Object(t)).forEach((function(n){Object.defineProperty(e,n,Object.getOwnPropertyDescriptor(t,n))}))}return e}function l(e,n){if(null==e)return{};var t,r,a=function(e,n){if(null==e)return{};var t,r,a={},i=Object.keys(e);for(r=0;r<i.length;r++)t=i[r],n.indexOf(t)>=0||(a[t]=e[t]);return a}(e,n);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(r=0;r<i.length;r++)t=i[r],n.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(a[t]=e[t])}return a}var u=r.createContext({}),s=function(e){var n=r.useContext(u),t=n;return e&&(t="function"==typeof e?e(n):o(o({},n),e)),t},c=function(e){var n=s(e.components);return r.createElement(u.Provider,{value:n},e.children)},d={inlineCode:"code",wrapper:function(e){var n=e.children;return r.createElement(r.Fragment,{},n)}},p=r.forwardRef((function(e,n){var t=e.components,a=e.mdxType,i=e.originalType,u=e.parentName,c=l(e,["components","mdxType","originalType","parentName"]),p=s(t),f=a,m=p["".concat(u,".").concat(f)]||p[f]||d[f]||i;return t?r.createElement(m,o(o({ref:n},c),{},{components:t})):r.createElement(m,o({ref:n},c))}));function f(e,n){var t=arguments,a=n&&n.mdxType;if("string"==typeof e||a){var i=t.length,o=new Array(i);o[0]=p;var l={};for(var u in n)hasOwnProperty.call(n,u)&&(l[u]=n[u]);l.originalType=e,l.mdxType="string"==typeof e?e:a,o[1]=l;for(var s=2;s<i;s++)o[s]=t[s];return r.createElement.apply(null,o)}return r.createElement.apply(null,t)}p.displayName="MDXCreateElement"},58215:function(e,n,t){var r=t(67294);n.Z=function(e){var n=e.children,t=e.hidden,a=e.className;return r.createElement("div",{role:"tabpanel",hidden:t,className:a},n)}},9877:function(e,n,t){t.d(n,{Z:function(){return c}});var r=t(83117),a=t(67294),i=t(72389),o=t(24726),l=t(86010),u="tabItem_LplD";function s(e){var n,t,i,s=e.lazy,c=e.block,d=e.defaultValue,p=e.values,f=e.groupId,m=e.className,b=a.Children.map(e.children,(function(e){if((0,a.isValidElement)(e)&&void 0!==e.props.value)return e;throw new Error("Docusaurus error: Bad <Tabs> child <"+("string"==typeof e.type?e.type:e.type.name)+'>: all children of the <Tabs> component should be <TabItem>, and every <TabItem> should have a unique "value" prop.')})),v=null!=p?p:b.map((function(e){var n=e.props;return{value:n.value,label:n.label,attributes:n.attributes}})),g=(0,o.lx)(v,(function(e,n){return e.value===n.value}));if(g.length>0)throw new Error('Docusaurus error: Duplicate values "'+g.map((function(e){return e.value})).join(", ")+'" found in <Tabs>. Every value needs to be unique.');var h=null===d?d:null!=(n=null!=d?d:null==(t=b.find((function(e){return e.props.default})))?void 0:t.props.value)?n:null==(i=b[0])?void 0:i.props.value;if(null!==h&&!v.some((function(e){return e.value===h})))throw new Error('Docusaurus error: The <Tabs> has a defaultValue "'+h+'" but none of its children has the corresponding value. Available values are: '+v.map((function(e){return e.value})).join(", ")+". If you intend to show no default tab, use defaultValue={null} instead.");var y=(0,o.UB)(),k=y.tabGroupChoices,w=y.setTabGroupChoices,O=(0,a.useState)(h),j=O[0],T=O[1],x=[],E=(0,o.o5)().blockElementScrollPositionUntilNextRender;if(null!=f){var C=k[f];null!=C&&C!==j&&v.some((function(e){return e.value===C}))&&T(C)}var D=function(e){var n=e.currentTarget,t=x.indexOf(n),r=v[t].value;r!==j&&(E(n),T(r),null!=f&&w(f,r))},N=function(e){var n,t=null;switch(e.key){case"ArrowRight":var r=x.indexOf(e.currentTarget)+1;t=x[r]||x[0];break;case"ArrowLeft":var a=x.indexOf(e.currentTarget)-1;t=x[a]||x[x.length-1]}null==(n=t)||n.focus()};return a.createElement("div",{className:"tabs-container"},a.createElement("ul",{role:"tablist","aria-orientation":"horizontal",className:(0,l.Z)("tabs",{"tabs--block":c},m)},v.map((function(e){var n=e.value,t=e.label,i=e.attributes;return a.createElement("li",(0,r.Z)({role:"tab",tabIndex:j===n?0:-1,"aria-selected":j===n,key:n,ref:function(e){return x.push(e)},onKeyDown:N,onFocus:D,onClick:D},i,{className:(0,l.Z)("tabs__item",u,null==i?void 0:i.className,{"tabs__item--active":j===n})}),null!=t?t:n)}))),s?(0,a.cloneElement)(b.filter((function(e){return e.props.value===j}))[0],{className:"margin-vert--md"}):a.createElement("div",{className:"margin-vert--md"},b.map((function(e,n){return(0,a.cloneElement)(e,{key:n,hidden:e.props.value!==j})}))))}function c(e){var n=(0,i.Z)();return a.createElement(s,(0,r.Z)({key:String(n)},e))}},41751:function(e,n,t){t.r(n),t.d(n,{frontMatter:function(){return s},contentTitle:function(){return c},metadata:function(){return d},toc:function(){return p},default:function(){return m}});var r=t(83117),a=t(80102),i=(t(67294),t(3905)),o=t(9877),l=t(58215),u=["components"],s={id:"suppressing-findings",title:"Suppressing Findings",sidebar_label:"Suppressing Findings"},c=void 0,d={unversionedId:"suppressing-findings",id:"suppressing-findings",title:"Suppressing Findings",description:"You can disable individual ModuleCheck findings via annotation, just like with any other lint tool.",source:"@site/docs/suppressing-findings.mdx",sourceDirName:".",slug:"/suppressing-findings",permalink:"/ModuleCheck/docs/next/suppressing-findings",editUrl:"https://github.com/rbusarow/ModuleCheck/docs/suppressing-findings.mdx",tags:[],version:"current",frontMatter:{id:"suppressing-findings",title:"Suppressing Findings",sidebar_label:"Suppressing Findings"},sidebar:"Docs",previous:{title:"Configuration",permalink:"/ModuleCheck/docs/next/configuration"},next:{title:"CI Workflow",permalink:"/ModuleCheck/docs/next/ci-workflow"}},p=[],f={toc:p};function m(e){var n=e.components,t=(0,a.Z)(e,u);return(0,i.kt)("wrapper",(0,r.Z)({},f,t,{components:n,mdxType:"MDXLayout"}),(0,i.kt)("p",null,"You can disable individual ModuleCheck findings via annotation, just like with any other lint tool."),(0,i.kt)("p",null,"The name of the check to disable can be found in the ",(0,i.kt)("inlineCode",{parentName:"p"},"name")," column of console output:"),(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre"},"> Task :moduleCheck\nModuleCheck found 3 issues in 6.157 seconds\n\n:app\n  dependency      name                  build file\n  :fat-and-leaky  inheritedDependency   /Users/rbusarow/projects/sample/app/build.gradle.kts: (15, 3):\n  :fat-and-leaky  mustBeApi             /Users/rbusarow/projects/sample/app/build.gradle.kts: (15, 3):\n  :unused-lib     unusedDependency      /Users/rbusarow/projects/sample/app/build.gradle.kts: (49, 3):\n\n")),(0,i.kt)(o.Z,{groupId:"language",defaultValue:"Kotlin",values:[{label:"Kotlin",value:"Kotlin"},{label:"Groovy",value:"Groovy"}],mdxType:"Tabs"},(0,i.kt)(l.Z,{value:"Kotlin",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-kotlin",metastring:'title="build.gradle.kts"',title:'"build.gradle.kts"'},'@Suppress("mustBeApi") // don\'t switch anything to an api config\ndependencies {\n\n  @Suppress("unusedDependency") // don\'t comment out or delete this dependency\n  implementation(project(":unused-lib"))\n\n  @Suppress("inheritedDependency") // don\'t add dependencies which are inherited from this fat jar\n  implementation(project(":fat-and-leaky"))\n}\n'))),(0,i.kt)(l.Z,{value:"Groovy",mdxType:"TabItem"},(0,i.kt)("pre",null,(0,i.kt)("code",{parentName:"pre",className:"language-groovy",metastring:'title="build.gradle"',title:'"build.gradle"'},'// don\'t switch anything to an api config\n//noinspection mustBeApi\ndependencies {\n\n  // don\'t comment out or delete this dependency\n  //noinspection unusedDependency\n  implementation(project(":unused-lib"))\n\n  // don\'t add dependencies which are inherited from this fat jar\n  //noinspection inheritedDependency\n  implementation(project(":fat-and-leaky"))\n}\n')))))}m.isMDXComponent=!0}}]);