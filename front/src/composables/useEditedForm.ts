import { computed, reactive, ref, type Ref } from 'vue';

// The values of an edit form, compared with the last saved ones: "Cancel" restores them and stays disabled
// while nothing differs, and a successful save makes the edited values the new saved ones.
export function useEditedForm<Values extends object>(values: Values) {
  const copyOf = (source: Values): Values => JSON.parse(JSON.stringify(source));

  const savedValues = ref(copyOf(values)) as Ref<Values>;
  const editedValues = reactive(copyOf(values)) as Values;
  const isModified = computed(() => JSON.stringify(editedValues) !== JSON.stringify(savedValues.value));

  const snapshot = () => copyOf(editedValues);
  const markSaved = () => {
    savedValues.value = copyOf(editedValues);
  };
  const restore = () => {
    Object.assign(editedValues, copyOf(savedValues.value));
  };

  return { editedValues, isModified, snapshot, markSaved, restore };
}
