import test from 'node:test';
import assert from 'node:assert/strict';
import {registrationError,hasFirebaseConfig,authError} from '../auth.js';
import {previewRegister,previewLogin,previewLogout,previewUser} from '../preview-auth.js';
const memory=()=>{const values=new Map();return {getItem:k=>values.get(k)??null,setItem:(k,v)=>values.set(k,v),removeItem:k=>values.delete(k)};};
const values={name:'Preview Learner',email:'learner@example.test',password:'SamplePassword123',confirmPassword:'SamplePassword123'};
test('registration requires valid details and matching passwords',()=>{
 assert.equal(registrationError(values),'');
 for(const change of [{name:''},{email:'invalid'},{password:'short'},{confirmPassword:'DifferentPassword'}])assert.ok(registrationError({...values,...change}));
});
test('Firebase requires its complete public web configuration',()=>{
 assert.equal(hasFirebaseConfig(null),false);assert.equal(hasFirebaseConfig({apiKey:'public'}),false);
 assert.equal(hasFirebaseConfig({apiKey:'public',authDomain:'sample.firebaseapp.com',projectId:'sample',appId:'app'}),true);
});
test('preview registration, logout and login work without storing a plain password',async()=>{
 const store=memory(),session=memory();
 const registered=await previewRegister(values,store,session);
 assert.equal(registered.user.displayName,values.name);assert.equal(registered.user.preview,true);
 assert.equal(previewUser(store,session).uid,registered.user.uid);
 assert.ok(!store.getItem('juafit-preview-accounts').includes(values.password));
 previewLogout(session);assert.equal(previewUser(store,session),null);
 const loggedIn=await previewLogin('LEARNER@example.test',values.password,store,session);
 assert.equal(loggedIn.user.uid,registered.user.uid);
});
test('preview rejects wrong passwords and duplicate email accounts',async()=>{
 const store=memory(),session=memory();await previewRegister(values,store,session);
 await assert.rejects(previewLogin(values.email,'WrongPassword',store,session),{code:'auth/invalid-credential'});
 await assert.rejects(previewRegister(values,store,session),{code:'auth/email-already-in-use'});
});
test('errors never expose the raw provider response',()=>{
 assert.equal(authError({code:'unknown',message:'Private internal details'}),'We could not complete sign-in. Please try again.');
});
