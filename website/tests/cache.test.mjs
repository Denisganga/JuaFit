import test from 'node:test';import assert from 'node:assert/strict';
import {cacheKey,readCache,writeCache} from '../response-cache.js';
const store=()=>({getItem(k){return this[k]||null},setItem(k,v){this[k]=v},removeItem(k){delete this[k]}});
test('completed results are reused until expiry',()=>{const s=store();writeCache(s,'key',{text:'Saved'},100,1000);assert.equal(readCache(s,'key',1099).text,'Saved');assert.equal(readCache(s,'key',1100),null);});
test('keys change with answers and account; chat is never cached',async()=>{assert.notEqual(await cacheKey('/api/analyze',{answers:[1]},'a'),await cacheKey('/api/analyze',{answers:[2]},'a'));assert.notEqual(await cacheKey('/api/analyze',{},'a'),await cacheKey('/api/analyze',{},'b'));assert.equal(await cacheKey('/api/chat',{},'a'),null);assert.equal(await cacheKey('/api/analyze',{},''),null);});
test('broken or blocked storage does not break guidance',()=>{assert.equal(readCache({getItem(){throw Error()}},'key'),null);assert.doesNotThrow(()=>writeCache({},'key',{},100));});
