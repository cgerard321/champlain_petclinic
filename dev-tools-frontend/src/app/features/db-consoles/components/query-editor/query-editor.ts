import {
  AfterViewInit,
  Component,
  ElementRef,
  OnDestroy,
  effect,
  input,
  model,
  viewChild,
} from '@angular/core';
import { Compartment, EditorState, Extension } from '@codemirror/state';
import { EditorView } from '@codemirror/view';
import { basicSetup } from 'codemirror';
import { sql, SQLDialect, StandardSQL } from '@codemirror/lang-sql';
import { json } from '@codemirror/lang-json';
import { QueryLanguage } from '@features/db-consoles/models/query-language';

@Component({
  selector: 'app-query-editor',
  templateUrl: './query-editor.html',
  styleUrl: './query-editor.css',
})
export class QueryEditor implements AfterViewInit, OnDestroy {
  readonly language = input<QueryLanguage>('sql');
  readonly dialect = input<SQLDialect>(StandardSQL);
  readonly value = model('');

  private readonly host = viewChild.required<ElementRef<HTMLDivElement>>('editorHost');
  private readonly languageCompartment = new Compartment();
  private view: EditorView | null = null;

  constructor() {
    effect(() => {
      const lang = this.language();
      const dialect = this.dialect();
      this.view?.dispatch({
        effects: this.languageCompartment.reconfigure(this.buildLanguageExtension(lang, dialect)),
      });
    });
  }

  ngAfterViewInit(): void {
    const state = EditorState.create({
      doc: this.value(),
      extensions: [
        basicSetup,
        this.languageCompartment.of(this.buildLanguageExtension(this.language(), this.dialect())),
        EditorView.updateListener.of((update) => {
          if (update.docChanged) {
            this.value.set(update.state.doc.toString());
          }
        }),
      ],
    });

    this.view = new EditorView({ state, parent: this.host().nativeElement });
  }

  ngOnDestroy(): void {
    this.view?.destroy();
  }

  private buildLanguageExtension(lang: QueryLanguage, dialect: SQLDialect): Extension {
    return lang === 'sql' ? sql({ dialect }) : json();
  }
}
